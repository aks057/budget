package com.budwiser.analytics.dto;

import java.math.BigDecimal;

/**
 * Current month (month-to-date when it is the running month) vs previous month vs the 3 months before it.
 */
public record CategoryComparisonDto(String categoryId, String categoryName, String categoryIcon, BigDecimal current,
                                    BigDecimal previous, BigDecimal threeMonthAverage,
                                    BigDecimal changeVsPreviousPercent, BigDecimal deviationVsAveragePercent) {}
