package com.budwiser.insight.service.impl;

import com.budwiser.analytics.dto.MonthlyTrendDto;
import com.budwiser.analytics.engine.GoalCalculator;
import com.budwiser.analytics.service.IAnalyticsService;
import com.budwiser.auth.entity.User;
import com.budwiser.auth.repository.IUserRepository;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.ResourceNotFoundException;
import com.budwiser.common.util.MoneyMath;
import com.budwiser.goal.repository.IGoalRepository;
import com.budwiser.insight.constant.InsightConstants;
import com.budwiser.insight.dto.InsightDtos.InsightDto;
import com.budwiser.insight.dto.InsightDtos.MarkAllReadDto;
import com.budwiser.insight.dto.InsightDtos.RefreshResultDto;
import com.budwiser.insight.dto.InsightDtos.UnreadCountDto;
import com.budwiser.insight.engine.InsightCandidate;
import com.budwiser.insight.engine.InsightFacts;
import com.budwiser.insight.engine.InsightRules;
import com.budwiser.insight.entity.Insight;
import com.budwiser.insight.repository.IInsightRepository;
import com.budwiser.insight.service.IInsightService;
import com.budwiser.security.service.ICurrentUserProvider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService implements IInsightService {
  private final IInsightRepository insightRepository;
  private final IAnalyticsService analyticsService;
  private final IGoalRepository goalRepository;
  private final IUserRepository userRepository;
  private final ICurrentUserProvider currentUserProvider;
  private final Clock clock;

  @Override
  @Transactional(readOnly = true)
  public List<InsightDto> list() {
    return insightRepository.findByUserIdOrderByIdDesc(currentUserProvider.getUserId(),
        PageRequest.of(0, InsightConstants.MAX_INSIGHTS_LISTED)).stream()
      .map(InsightService::toDto)
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public UnreadCountDto unreadCount() {
    return new UnreadCountDto(insightRepository.countByUserIdAndReadAtIsNull(currentUserProvider.getUserId()));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void markRead(Long insightId) {
    Long userId = currentUserProvider.getUserId();
    Insight insight = insightRepository.findByIdAndUserId(insightId, userId)
      .orElseThrow(() -> new ResourceNotFoundException(insightId, ErrorCode.INSIGHT_NOT_FOUND));
    if (insight.getReadAt() == null) {
      insight.setReadAt(clock.millis());
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public MarkAllReadDto markAllRead() {
    return new MarkAllReadDto(insightRepository.markAllRead(currentUserProvider.getUserId(), clock.millis()));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public RefreshResultDto refresh() {
    return new RefreshResultDto(generateFor(currentUserProvider.getUserId()));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int generateFor(Long userId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      return 0;
    }
    LocalDate today = LocalDate.now(clock);
    YearMonth month = YearMonth.from(today);
    InsightFacts facts = new InsightFacts(today, user.getCurrency(),
      analyticsService.anomalies(userId, month),
      analyticsService.recurringExpenses(userId),
      goalRepository.findByUserIdOrderByTargetDateAsc(userId).stream()
        .map(goal -> new InsightFacts.GoalFact(goal.getId(), goal.getName(), goal.getTargetDate(),
          Instant.ofEpochMilli(goal.getModifiedAt()).atZone(clock.getZone()).toLocalDate(),
          GoalCalculator.progress(goal.getTargetAmount(), goal.getCurrentAmount(), goal.getTargetDate(), month)))
        .toList(),
      averageMonthlySavings(userId));

    long now = clock.millis();
    int created = 0;
    for (InsightCandidate candidate : InsightRules.evaluate(facts)) {
      created += insightRepository.insertIfAbsent(userId, candidate.type().name(), candidate.severity().name(),
        candidate.title(), candidate.body(), candidate.dedupeKey(), now);
    }
    if (created > 0) {
      log.info("[generateFor] insights created, userId: {}, created: {}", userId, created);
    }
    return created;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int purgeCreatedBefore(long cutoffEpochMillis) {
    return insightRepository.deleteCreatedBefore(cutoffEpochMillis);
  }

  /**
   * Average savings over the last full months (the current, partial month would understate it).
   * Null when those months have no activity at all: no history means no basis for "behind schedule".
   */
  private BigDecimal averageMonthlySavings(Long userId) {
    List<MonthlyTrendDto> trends = analyticsService.trends(userId, InsightConstants.SAVINGS_PACE_MONTHS + 1);
    List<MonthlyTrendDto> fullMonths = trends.subList(0, Math.max(trends.size() - 1, 0));
    boolean hasActivity = fullMonths.stream()
      .anyMatch(month -> month.income().signum() != 0 || month.expense().signum() != 0);
    if (!hasActivity) {
      return null;
    }
    BigDecimal total = fullMonths.stream().map(MonthlyTrendDto::savings).reduce(BigDecimal.ZERO, BigDecimal::add);
    return MoneyMath.average(total, fullMonths.size());
  }

  private static InsightDto toDto(Insight insight) {
    return new InsightDto(String.valueOf(insight.getId()), insight.getType(), insight.getSeverity(), insight.getTitle(),
      insight.getBody(), insight.getReadAt() != null, insight.getCreatedAt());
  }
}
