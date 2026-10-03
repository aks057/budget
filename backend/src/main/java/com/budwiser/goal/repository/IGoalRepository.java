package com.budwiser.goal.repository;

import com.budwiser.goal.entity.FinancialGoal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IGoalRepository extends JpaRepository<FinancialGoal, Long> {

  Optional<FinancialGoal> findByIdAndUserId(Long id, Long userId);

  /** Bounded by GoalConstants.MAX_GOALS_PER_USER. */
  List<FinancialGoal> findByUserIdOrderByTargetDateAsc(Long userId);

  long countByUserId(Long userId);
}
