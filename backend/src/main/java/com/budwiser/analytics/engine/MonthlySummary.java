package com.budwiser.analytics.engine;

import java.math.BigDecimal;

/**
 * @param savingsRate savings / income × 100; null when there is no income (rate is undefined, not 0%)
 */
public record MonthlySummary(BigDecimal income, BigDecimal expense, BigDecimal savings, BigDecimal savingsRate) {}
