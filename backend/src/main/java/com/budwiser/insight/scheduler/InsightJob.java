package com.budwiser.insight.scheduler;

import com.budwiser.auth.repository.IUserRepository;
import com.budwiser.insight.constant.InsightConstants;
import com.budwiser.insight.service.IInsightService;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * One full insights run: every user, then retention cleanup.
 *
 * <ul>
 *   <li>Each user is generated in its own short transaction (IInsightService.generateFor), so one user's failure
 *       is logged and skipped instead of aborting the run, and no transaction spans the whole user base.</li>
 *   <li>Users are walked with keyset paging on id: memory stays flat and users registering mid-run don't shift
 *       pages.</li>
 *   <li>Safe to run twice (or concurrently on two instances): inserts are idempotent per dedupe key. At most the
 *       work is duplicated, never the insights. ShedLock would avoid the duplicate work once we run replicas.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsightJob {
  private final IUserRepository userRepository;
  private final IInsightService insightService;
  private final Clock clock;

  public RunSummary run() {
    long startedAt = clock.millis();
    int users = 0;
    int created = 0;
    int failed = 0;
    Long afterId = 0L;
    List<Long> batch;
    do {
      batch = userRepository.findIdsAfter(afterId, PageRequest.of(0, InsightConstants.USER_BATCH_SIZE));
      for (Long userId : batch) {
        users++;
        try {
          created += insightService.generateFor(userId);
        } catch (RuntimeException ex) {
          failed++;
          log.error("[run] insight generation failed, userId: {}", userId, ex);
        }
      }
      if (!batch.isEmpty()) {
        afterId = batch.getLast();
      }
    } while (batch.size() == InsightConstants.USER_BATCH_SIZE);

    long cutoff = clock.millis() - Duration.ofDays(InsightConstants.RETENTION_DAYS).toMillis();
    int purged = insightService.purgeCreatedBefore(cutoff);
    RunSummary summary = new RunSummary(users, created, failed, purged, clock.millis() - startedAt);
    log.info("[run] insights job finished, users: {}, created: {}, failed: {}, purged: {}, tookMs: {}",
      summary.users(), summary.created(), summary.failed(), summary.purged(), summary.tookMs());
    return summary;
  }

  public record RunSummary(int users, int created, int failed, int purged, long tookMs) {}
}
