package com.budwiser.analytics.engine;

import java.math.BigDecimal;

/**
 * A detected fact, not prose: the LLM later only phrases it (PRD §17 — numbers come from the backend).
 *
 * @param actual           observed value (month-to-date spend, budget spend, or transaction amount)
 * @param baseline         what it is compared against (3-month average, budget limit, or category median)
 * @param deviationPercent for budgets: percent used; otherwise percent above baseline
 * @param transactionId    only for LARGE_TRANSACTION
 */
public record Anomaly(AnomalyType type, AnomalySeverity severity, Long categoryId, Long transactionId,
                      BigDecimal actual, BigDecimal baseline, BigDecimal deviationPercent) {}
