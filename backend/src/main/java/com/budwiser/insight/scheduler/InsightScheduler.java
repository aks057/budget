package com.budwiser.insight.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cron trigger for {@link InsightJob}, evaluated in the business timezone (default 08:00 Asia/Kolkata).
 * Disabled with budwiser.insights.enabled=false (the test profile does this; tests call the job directly).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "budwiser.insights", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InsightScheduler {
  private final InsightJob insightJob;

  @Scheduled(cron = "${budwiser.insights.cron:0 0 8 * * *}", zone = "${budwiser.app.zone:Asia/Kolkata}")
  public void daily() {
    insightJob.run();
  }
}
