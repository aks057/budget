package com.budwiser.transaction.repository;

import com.budwiser.transaction.constant.TransactionQueries;
import com.budwiser.transaction.entity.Transaction;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, Long> {

  Optional<Transaction> findByIdAndUserId(Long id, Long userId);

  boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);

  @Query(value = TransactionQueries.SEARCH, countQuery = TransactionQueries.COUNT_SEARCH, nativeQuery = true)
  Page<Transaction> search(@Param("userId") Long userId,
                           @Param("from") LocalDate from,
                           @Param("to") LocalDate to,
                           @Param("type") String type,
                           @Param("categoryId") Long categoryId,
                           Pageable pageable);
}
