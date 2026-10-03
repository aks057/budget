package com.budwiser.insight.engine;

import com.budwiser.analytics.dto.AnomalyDto;
import com.budwiser.analytics.dto.RecurringExpenseDto;
import com.budwiser.analytics.engine.GoalProgress;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Everything the insight rules need for one user, already computed by the analytics engine.
 *
 * @param averageMonthlySavings average savings over the last full months; null when there is no history to judge by
 */
public record InsightFacts(LocalDate today, String currency, List<AnomalyDto> anomalies,
                           List<RecurringExpenseDto> recurringExpenses, List<GoalFact> goals,
                           BigDecimal averageMonthlySavings) {

  /** @param updatedOn day the goal was last changed (an achieved goal is only announced right after it is reached) */
  public record GoalFact(Long id, String name, LocalDate targetDate, LocalDate updatedOn, GoalProgress progress) {}
}
