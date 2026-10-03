package com.budwiser.budget.service.impl;

import com.budwiser.analytics.engine.BudgetCalculator;
import com.budwiser.analytics.repository.CategoryTotalView;
import com.budwiser.analytics.repository.IAnalyticsRepository;
import com.budwiser.budget.dto.BudgetDto;
import com.budwiser.budget.dto.CreateBudgetRequest;
import com.budwiser.budget.dto.UpdateBudgetRequest;
import com.budwiser.budget.entity.Budget;
import com.budwiser.budget.mapper.IBudgetMapper;
import com.budwiser.budget.repository.IBudgetRepository;
import com.budwiser.budget.service.IBudgetService;
import com.budwiser.category.entity.Category;
import com.budwiser.category.service.ICategoryService;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.exception.ConflictException;
import com.budwiser.common.exception.ResourceNotFoundException;
import com.budwiser.common.exception.ValidationException;
import com.budwiser.common.util.DateRange;
import com.budwiser.security.service.ICurrentUserProvider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService implements IBudgetService {
  private final IBudgetRepository budgetRepository;
  private final IAnalyticsRepository analyticsRepository;
  private final ICategoryService categoryService;
  private final IBudgetMapper budgetMapper;
  private final ICurrentUserProvider currentUserProvider;
  private final Clock clock;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public BudgetDto create(CreateBudgetRequest request) {
    Long userId = currentUserProvider.getUserId();
    Category category = getExpenseCategory(userId, request.getCategoryId());
    if (budgetRepository.existsByUserIdAndCategoryId(userId, category.getId())) {
      throw new ConflictException(ErrorCode.BUDGET_ALREADY_EXISTS);
    }
    Budget budget = new Budget();
    budget.setUserId(userId);
    budget.setCategoryId(category.getId());
    budget.setAmount(request.getAmount());
    budgetRepository.save(budget);
    log.info("[create] budget created, userId: {}, budgetId: {}", userId, budget.getId());
    return toDtoForCurrentMonth(userId, budget, category);
  }

  /** Three queries total: budgets, the month's expense totals per category, and the user's categories. */
  @Override
  @Transactional(readOnly = true)
  public List<BudgetDto> list(YearMonth month) {
    return getStatuses(currentUserProvider.getUserId(), month);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BudgetDto> getStatuses(Long userId, YearMonth month) {
    YearMonth targetMonth = month != null ? month : YearMonth.now(clock);
    Map<Long, BigDecimal> spentByCategory = spentByCategory(userId, targetMonth);
    Map<Long, Category> categories = categoryService.getCategoriesById(userId);

    return budgetRepository.findByUserId(userId).stream()
      .map(budget -> budgetMapper.toDto(budget, categories.get(budget.getCategoryId()),
        BudgetCalculator.calculate(budget.getAmount(), spentByCategory.get(budget.getCategoryId())), targetMonth))
      .sorted(Comparator.comparing(BudgetDto::getPercentUsed).reversed())
      .toList();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public BudgetDto update(Long id, UpdateBudgetRequest request) {
    Long userId = currentUserProvider.getUserId();
    Budget budget = getOwned(userId, id);
    budget.setAmount(request.getAmount());
    log.info("[update] budget updated, userId: {}, budgetId: {}", userId, id);
    return toDtoForCurrentMonth(userId, budget, categoryService.getOwnedCategory(userId, budget.getCategoryId()));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id) {
    Long userId = currentUserProvider.getUserId();
    budgetRepository.delete(getOwned(userId, id));
    log.info("[delete] budget deleted, userId: {}, budgetId: {}", userId, id);
  }

  private BudgetDto toDtoForCurrentMonth(Long userId, Budget budget, Category category) {
    YearMonth currentMonth = YearMonth.now(clock);
    BigDecimal spent = spentByCategory(userId, currentMonth).get(budget.getCategoryId());
    return budgetMapper.toDto(budget, category, BudgetCalculator.calculate(budget.getAmount(), spent), currentMonth);
  }

  private Map<Long, BigDecimal> spentByCategory(Long userId, YearMonth month) {
    DateRange range = DateRange.ofMonth(month);
    return analyticsRepository.totalsByCategory(userId, range.from(), range.toExclusive(), TransactionType.EXPENSE.name())
      .stream()
      .collect(Collectors.toMap(CategoryTotalView::getCategoryId, CategoryTotalView::getTotal));
  }

  private Category getExpenseCategory(Long userId, Long categoryId) {
    Category category = categoryService.getOwnedCategory(userId, categoryId);
    if (category.getType() != TransactionType.EXPENSE) {
      throw new ValidationException(ErrorCode.BUDGET_CATEGORY_NOT_EXPENSE);
    }
    return category;
  }

  private Budget getOwned(Long userId, Long id) {
    return budgetRepository.findByIdAndUserId(id, userId)
      .orElseThrow(() -> new ResourceNotFoundException(id, ErrorCode.BUDGET_NOT_FOUND));
  }
}
