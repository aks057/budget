package com.budwiser.insight.dto;

import com.budwiser.insight.constant.InsightSeverity;
import com.budwiser.insight.constant.InsightType;

/**
 * Response shapes of the insights API (immutable read models).
 */
public final class InsightDtos {
  private InsightDtos() {}

  /** @param createdAt epoch millis */
  public record InsightDto(String id, InsightType type, InsightSeverity severity, String title, String body,
                           boolean read, long createdAt) {}

  public record UnreadCountDto(long count) {}

  /** @param created insights newly raised by this run (0 when everything was already known) */
  public record RefreshResultDto(int created) {}

  public record MarkAllReadDto(int updated) {}
}
