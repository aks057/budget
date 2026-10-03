package com.budwiser.analytics.engine;

import java.math.BigDecimal;

/**
 * @param monthsRemaining             whole months from the current month to the target month (0 = due this month)
 * @param requiredMonthlyContribution remaining spread over the months left, rounded UP to the paisa so the goal is met
 * @param overdue                     target month has passed and the goal is not achieved
 */
public record GoalProgress(BigDecimal remaining, BigDecimal percentComplete, long monthsRemaining,
                           BigDecimal requiredMonthlyContribution, boolean achieved, boolean overdue) {}
