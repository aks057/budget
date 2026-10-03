package com.budwiser.insight.service;

import com.budwiser.insight.dto.InsightDtos.InsightDto;
import com.budwiser.insight.dto.InsightDtos.MarkAllReadDto;
import com.budwiser.insight.dto.InsightDtos.RefreshResultDto;
import com.budwiser.insight.dto.InsightDtos.UnreadCountDto;
import java.util.List;

public interface IInsightService {

  /** The current user's most recent insights, newest first. */
  List<InsightDto> list();

  UnreadCountDto unreadCount();

  /** Idempotent: marking an already-read insight is a no-op. 404 for another user's insight. */
  void markRead(Long insightId);

  MarkAllReadDto markAllRead();

  /** Runs the insight rules for the current user now (instead of waiting for the daily job). */
  RefreshResultDto refresh();

  /**
   * Evaluates the rules for one user and stores new insights. Takes an explicit userId because the daily job runs
   * without a request; the id comes from the users table, never from a client.
   *
   * @return number of insights newly created
   */
  int generateFor(Long userId);

  /** Deletes insights created before {@code cutoffEpochMillis}; returns the number deleted. */
  int purgeCreatedBefore(long cutoffEpochMillis);
}
