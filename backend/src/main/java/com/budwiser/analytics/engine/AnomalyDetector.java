package com.budwiser.analytics.engine;

import com.budwiser.common.util.MoneyMath;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Deterministic anomaly rules (PRD §20). Pure function of its inputs — easy to test, explain and tune.
 * Example: 3-month average ₹7,000, this month ₹10,500 → deviation 50% > 30% → SPENDING_SPIKE.
 */
public final class AnomalyDetector {
  private AnomalyDetector() {}

  /** Spike when month-to-date spend exceeds the 3-month average by more than this. */
  public static final BigDecimal SPIKE_DEVIATION_PERCENT = BigDecimal.valueOf(30);
  /** Spikes above this deviation are CRITICAL. */
  public static final BigDecimal SPIKE_CRITICAL_PERCENT = BigDecimal.valueOf(75);
  /** Ignore categories whose average is tiny (₹200 → ₹300 is +50% but not interesting). */
  public static final BigDecimal SPIKE_MIN_BASELINE = BigDecimal.valueOf(500);
  /** A transaction is "large" above this multiple of the category's median. */
  public static final BigDecimal LARGE_TRANSACTION_MULTIPLIER = BigDecimal.valueOf(3);
  /** Need this many peer transactions before a median is meaningful. */
  public static final int LARGE_TRANSACTION_MIN_PEERS = 5;

  public record CategorySpend(Long categoryId, BigDecimal monthToDate, BigDecimal threeMonthAverage) {}

  public record BudgetCheck(Long categoryId, BudgetStatus status) {}

  /**
   * @param recentExpenses expenses from the lookback window (e.g. 90 days) through today; those on/after
   *                       {@code monthStart} are candidates for LARGE_TRANSACTION, all others are peers
   */
  public static List<Anomaly> detect(List<CategorySpend> categorySpends, List<BudgetCheck> budgets,
                                     List<ExpenseItem> recentExpenses, LocalDate monthStart) {
    List<Anomaly> anomalies = new ArrayList<>();
    categorySpends.forEach(spend -> spendingSpike(spend).ifPresent(anomalies::add));
    budgets.forEach(budget -> budgetThreshold(budget).ifPresent(anomalies::add));
    anomalies.addAll(largeTransactions(recentExpenses, monthStart));
    anomalies.sort(Comparator.comparing(Anomaly::severity).reversed()
      .thenComparing(Anomaly::deviationPercent, Comparator.nullsLast(Comparator.reverseOrder())));
    return anomalies;
  }

  private static Optional<Anomaly> spendingSpike(CategorySpend spend) {
    BigDecimal average = MoneyMath.zeroIfNull(spend.threeMonthAverage());
    if (average.compareTo(SPIKE_MIN_BASELINE) < 0) {
      return Optional.empty();
    }
    BigDecimal deviation = MoneyMath.changePercent(MoneyMath.zeroIfNull(spend.monthToDate()), average);
    if (deviation == null || deviation.compareTo(SPIKE_DEVIATION_PERCENT) <= 0) {
      return Optional.empty();
    }
    AnomalySeverity severity = deviation.compareTo(SPIKE_CRITICAL_PERCENT) > 0
      ? AnomalySeverity.CRITICAL : AnomalySeverity.WARNING;
    return Optional.of(new Anomaly(AnomalyType.SPENDING_SPIKE, severity, spend.categoryId(), null,
      MoneyMath.money(spend.monthToDate()), MoneyMath.money(average), deviation));
  }

  private static Optional<Anomaly> budgetThreshold(BudgetCheck budget) {
    BudgetStatus status = budget.status();
    AnomalySeverity severity = switch (status.level()) {
      case EXCEEDED -> AnomalySeverity.CRITICAL;
      case WARNING -> AnomalySeverity.WARNING;
      case ON_TRACK -> null;
    };
    if (severity == null) {
      return Optional.empty();
    }
    return Optional.of(new Anomaly(AnomalyType.BUDGET_THRESHOLD, severity, budget.categoryId(), null,
      status.spent(), status.limit(), status.percentUsed()));
  }

  private static List<Anomaly> largeTransactions(List<ExpenseItem> recentExpenses, LocalDate monthStart) {
    Map<Long, List<ExpenseItem>> byCategory = recentExpenses.stream()
      .collect(Collectors.groupingBy(ExpenseItem::categoryId));
    List<Anomaly> anomalies = new ArrayList<>();
    for (ExpenseItem candidate : recentExpenses) {
      if (candidate.date().isBefore(monthStart)) {
        continue;
      }
      List<BigDecimal> peers = byCategory.get(candidate.categoryId()).stream()
        .filter(item -> !item.transactionId().equals(candidate.transactionId()))
        .map(ExpenseItem::amount)
        .toList();
      if (peers.size() < LARGE_TRANSACTION_MIN_PEERS) {
        continue;
      }
      BigDecimal median = MoneyMath.median(peers);
      if (median.signum() > 0 && candidate.amount().compareTo(median.multiply(LARGE_TRANSACTION_MULTIPLIER)) > 0) {
        anomalies.add(new Anomaly(AnomalyType.LARGE_TRANSACTION, AnomalySeverity.WARNING, candidate.categoryId(),
          candidate.transactionId(), MoneyMath.money(candidate.amount()), median,
          MoneyMath.changePercent(candidate.amount(), median)));
      }
    }
    return anomalies;
  }
}
