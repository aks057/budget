package com.budwiser.insight.engine;

import com.budwiser.insight.constant.InsightSeverity;
import com.budwiser.insight.constant.InsightType;

/**
 * An insight the rules want to raise. {@code dedupeKey} identifies the underlying fact so the same fact is stored
 * at most once per user.
 */
public record InsightCandidate(InsightType type, InsightSeverity severity, String title, String body, String dedupeKey) {}
