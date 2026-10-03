package com.budwiser.budget.repository;

import com.budwiser.budget.entity.Budget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBudgetRepository extends JpaRepository<Budget, Long> {

  Optional<Budget> findByIdAndUserId(Long id, Long userId);

  /** Bounded: at most one budget per expense category, and categories are capped per user. */
  List<Budget> findByUserId(Long userId);

  boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);
}
