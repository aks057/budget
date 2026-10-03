package com.budwiser.analytics.engine;

import java.math.BigDecimal;

/**
 * @param changeVsPreviousPercent   null when there was no spending last month
 * @param deviationVsAveragePercent null when the 3-month average is zero (no history)
 */
public record SpendingComparison(BigDecimal current, BigDecimal previous, BigDecimal threeMonthAverage,
                                 BigDecimal changeVsPreviousPercent, BigDecimal deviationVsAveragePercent) {}
