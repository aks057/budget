package com.budwiser.analytics.dto;

import com.budwiser.analytics.engine.AnomalySeverity;
import com.budwiser.analytics.engine.AnomalyType;
import java.math.BigDecimal;

/**
 * Facts only. Phase 4 turns these into human-readable insights (the LLM phrases, it does not compute).
 */
public record AnomalyDto(AnomalyType type, AnomalySeverity severity, String categoryId, String categoryName,
                         String transactionId, BigDecimal actual, BigDecimal baseline, BigDecimal deviationPercent) {}
