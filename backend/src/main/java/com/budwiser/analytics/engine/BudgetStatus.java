package com.budwiser.analytics.engine;

import java.math.BigDecimal;

/**
 * @param remaining limit − spent; negative means overspent by that amount
 */
public record BudgetStatus(BigDecimal limit, BigDecimal spent, BigDecimal remaining, BigDecimal percentUsed,
                           BudgetLevel level) {}
