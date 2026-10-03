package com.budwiser.goal.service.impl;

import com.budwiser.analytics.engine.GoalCalculator;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.ResourceNotFoundException;
import com.budwiser.common.exception.ValidationException;
import com.budwiser.goal.constant.GoalConstants;
import com.budwiser.goal.dto.GoalDto;
import com.budwiser.goal.dto.GoalRequest;
import com.budwiser.goal.entity.FinancialGoal;
import com.budwiser.goal.mapper.IGoalMapper;
import com.budwiser.goal.repository.IGoalRepository;
import com.budwiser.goal.service.IGoalService;
import com.budwiser.security.service.ICurrentUserProvider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService implements IGoalService {
  private final IGoalRepository goalRepository;
  private final IGoalMapper goalMapper;
  private final ICurrentUserProvider currentUserProvider;
  private final Clock clock;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public GoalDto create(GoalRequest request) {
    Long userId = currentUserProvider.getUserId();
    if (goalRepository.countByUserId(userId) >= GoalConstants.MAX_GOALS_PER_USER) {
      throw new ValidationException(ErrorCode.GOAL_LIMIT_REACHED);
    }
    validateTargetDate(request.getTargetDate());
    FinancialGoal goal = new FinancialGoal();
    goal.setUserId(userId);
    apply(goal, request);
    goalRepository.save(goal);
    log.info("[create] goal created, userId: {}, goalId: {}", userId, goal.getId());
    return toDto(goal);
  }

  @Override
  @Transactional(readOnly = true)
  public List<GoalDto> list() {
    return goalRepository.findByUserIdOrderByTargetDateAsc(currentUserProvider.getUserId()).stream()
      .map(this::toDto)
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public GoalDto getById(Long id) {
    return toDto(getOwned(currentUserProvider.getUserId(), id));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public GoalDto update(Long id, GoalRequest request) {
    Long userId = currentUserProvider.getUserId();
    FinancialGoal goal = getOwned(userId, id);
    validateTargetDate(request.getTargetDate());
    apply(goal, request);
    log.info("[update] goal updated, userId: {}, goalId: {}", userId, id);
    return toDto(goal);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id) {
    Long userId = currentUserProvider.getUserId();
    goalRepository.delete(getOwned(userId, id));
    log.info("[delete] goal deleted, userId: {}, goalId: {}", userId, id);
  }

  private GoalDto toDto(FinancialGoal goal) {
    return goalMapper.toDto(goal, GoalCalculator.progress(goal.getTargetAmount(), goal.getCurrentAmount(),
      goal.getTargetDate(), YearMonth.now(clock)));
  }

  private void validateTargetDate(LocalDate targetDate) {
    if (targetDate.isBefore(LocalDate.now(clock))) {
      throw new ValidationException(ErrorCode.GOAL_DATE_IN_PAST);
    }
  }

  private FinancialGoal getOwned(Long userId, Long id) {
    return goalRepository.findByIdAndUserId(id, userId)
      .orElseThrow(() -> new ResourceNotFoundException(id, ErrorCode.GOAL_NOT_FOUND));
  }

  private static void apply(FinancialGoal goal, GoalRequest request) {
    goal.setName(request.getName().trim());
    goal.setTargetAmount(request.getTargetAmount());
    goal.setCurrentAmount(request.getCurrentAmount() == null ? BigDecimal.ZERO : request.getCurrentAmount());
    goal.setTargetDate(request.getTargetDate());
  }
}
