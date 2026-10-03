package com.budwiser.transaction.service.impl;

import com.budwiser.category.entity.Category;
import com.budwiser.category.service.ICategoryService;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.ResourceNotFoundException;
import com.budwiser.common.exception.ValidationException;
import com.budwiser.common.util.DateRange;
import com.budwiser.security.service.ICurrentUserProvider;
import com.budwiser.transaction.dto.TransactionDto;
import com.budwiser.transaction.dto.TransactionQuery;
import com.budwiser.transaction.dto.TransactionRequest;
import com.budwiser.transaction.entity.Transaction;
import com.budwiser.transaction.mapper.ITransactionMapper;
import com.budwiser.transaction.repository.ITransactionRepository;
import com.budwiser.transaction.service.ITransactionService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
  private final ITransactionRepository transactionRepository;
  private final ICategoryService categoryService;
  private final ITransactionMapper transactionMapper;
  private final ICurrentUserProvider currentUserProvider;
  private final Clock clock;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public TransactionDto create(TransactionRequest request) {
    Long userId = currentUserProvider.getUserId();
    Category category = categoryService.getOwnedCategory(userId, request.getCategoryId());
    validateDate(request.getTransactionDate());

    Transaction transaction = new Transaction();
    transaction.setUserId(userId);
    apply(transaction, request, category);
    transactionRepository.save(transaction);
    log.info("[create] transaction created, userId: {}, transactionId: {}, type: {}",
      userId, transaction.getId(), transaction.getType());
    return transactionMapper.toDto(transaction, category);
  }

  @Override
  @Transactional(readOnly = true)
  public TransactionDto getById(Long id) {
    Long userId = currentUserProvider.getUserId();
    Transaction transaction = getOwned(userId, id);
    return transactionMapper.toDto(transaction, categoryService.getOwnedCategory(userId, transaction.getCategoryId()));
  }

  /** Two queries per page regardless of size: the page itself and the user's (bounded) category set. */
  @Override
  @Transactional(readOnly = true)
  public Page<TransactionDto> list(TransactionQuery query) {
    Long userId = currentUserProvider.getUserId();
    DateRange range = DateRange.resolve(query.getFrom(), query.getTo(), YearMonth.now(clock));
    String type = query.getType() == null ? null : query.getType().name();

    Page<Transaction> page = transactionRepository.search(userId, range.from(), range.to(), type,
      query.getCategoryId(), PageRequest.of(query.getPage(), query.getSize()));
    Map<Long, Category> categories = categoryService.getCategoriesById(userId);
    return page.map(transaction -> transactionMapper.toDto(transaction, categories.get(transaction.getCategoryId())));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public TransactionDto update(Long id, TransactionRequest request) {
    Long userId = currentUserProvider.getUserId();
    Transaction transaction = getOwned(userId, id);
    Category category = categoryService.getOwnedCategory(userId, request.getCategoryId());
    validateDate(request.getTransactionDate());
    apply(transaction, request, category);
    log.info("[update] transaction updated, userId: {}, transactionId: {}", userId, id);
    return transactionMapper.toDto(transaction, category);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id) {
    Long userId = currentUserProvider.getUserId();
    transactionRepository.delete(getOwned(userId, id));
    log.info("[delete] transaction deleted, userId: {}, transactionId: {}", userId, id);
  }

  private Transaction getOwned(Long userId, Long id) {
    return transactionRepository.findByIdAndUserId(id, userId)
      .orElseThrow(() -> new ResourceNotFoundException(id, ErrorCode.TRANSACTION_NOT_FOUND));
  }

  /** The clock is in the business timezone, so "today" is the user's date, not the server's UTC date. */
  private void validateDate(LocalDate transactionDate) {
    if (transactionDate.isAfter(LocalDate.now(clock))) {
      throw new ValidationException(ErrorCode.TRANSACTION_DATE_IN_FUTURE);
    }
  }

  private static void apply(Transaction transaction, TransactionRequest request, Category category) {
    transaction.setCategoryId(category.getId());
    transaction.setType(category.getType());
    transaction.setAmount(request.getAmount());
    transaction.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
    transaction.setTransactionDate(request.getTransactionDate());
  }
}
