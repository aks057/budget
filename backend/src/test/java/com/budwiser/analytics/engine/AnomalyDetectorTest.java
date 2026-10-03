package com.budwiser.analytics.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.budwiser.analytics.engine.AnomalyDetector.BudgetCheck;
import com.budwiser.analytics.engine.AnomalyDetector.CategorySpend;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnomalyDetectorTest {
  private static final LocalDate MONTH_START = LocalDate.of(2026, 10, 1);
  private static final Long FOOD = 1L;
  private static final Long SHOPPING = 2L;

  @Test
  @DisplayName("PRD example: 10,500 vs 3-month average 7,000 (+50%) → SPENDING_SPIKE, WARNING")
  void spendingSpike() {
    List<Anomaly> actual = AnomalyDetector.detect(
      List.of(new CategorySpend(SHOPPING, new BigDecimal("10500"), new BigDecimal("7000"))), List.of(), List.of(), MONTH_START);

    assertThat(actual).singleElement().satisfies(anomaly -> {
      assertThat(anomaly.type()).isEqualTo(AnomalyType.SPENDING_SPIKE);
      assertThat(anomaly.severity()).isEqualTo(AnomalySeverity.WARNING);
      assertThat(anomaly.deviationPercent()).isEqualByComparingTo("50.0");
      assertThat(anomaly.baseline()).isEqualByComparingTo("7000.00");
    });
  }

  @Test
  @DisplayName("spikes are thresholded: exactly +30% is not flagged, +80% is CRITICAL, tiny baselines are ignored")
  void spikeThresholds() {
    List<Anomaly> actual = AnomalyDetector.detect(List.of(
        new CategorySpend(FOOD, new BigDecimal("1300"), new BigDecimal("1000")),
        new CategorySpend(SHOPPING, new BigDecimal("1800"), new BigDecimal("1000")),
        new CategorySpend(3L, new BigDecimal("400"), new BigDecimal("200"))),
      List.of(), List.of(), MONTH_START);

    assertThat(actual).singleElement().satisfies(anomaly -> {
      assertThat(anomaly.categoryId()).isEqualTo(SHOPPING);
      assertThat(anomaly.severity()).isEqualTo(AnomalySeverity.CRITICAL);
    });
  }

  @Test
  @DisplayName("budgets at WARNING/EXCEEDED become anomalies; ON_TRACK budgets do not; most severe first")
  void budgetThresholds() {
    List<Anomaly> actual = AnomalyDetector.detect(List.of(), List.of(
        new BudgetCheck(FOOD, BudgetCalculator.calculate(new BigDecimal("10000"), new BigDecimal("8400"))),
        new BudgetCheck(SHOPPING, BudgetCalculator.calculate(new BigDecimal("5000"), new BigDecimal("6000"))),
        new BudgetCheck(3L, BudgetCalculator.calculate(new BigDecimal("5000"), new BigDecimal("100")))),
      List.of(), MONTH_START);

    assertThat(actual).extracting(Anomaly::categoryId).containsExactly(SHOPPING, FOOD);
    assertThat(actual.getFirst().severity()).isEqualTo(AnomalySeverity.CRITICAL);
    assertThat(actual.getFirst().deviationPercent()).isEqualByComparingTo("120.0");
  }

  @Test
  @DisplayName("a current-month expense above 3× the category median (≥5 peers) is a LARGE_TRANSACTION")
  void largeTransaction() {
    List<ExpenseItem> expenses = new ArrayList<>(peers(FOOD, "400", 5, MONTH_START.minusDays(40)));
    expenses.add(new ExpenseItem(99L, FOOD, new BigDecimal("2500"), MONTH_START.plusDays(3), "Wedding gift dinner"));

    List<Anomaly> actual = AnomalyDetector.detect(List.of(), List.of(), expenses, MONTH_START);

    assertThat(actual).singleElement().satisfies(anomaly -> {
      assertThat(anomaly.type()).isEqualTo(AnomalyType.LARGE_TRANSACTION);
      assertThat(anomaly.transactionId()).isEqualTo(99L);
      assertThat(anomaly.baseline()).isEqualByComparingTo("400.00");
      assertThat(anomaly.deviationPercent()).isEqualByComparingTo("525.0");
    });
  }

  @Test
  @DisplayName("not enough history (fewer than 5 peers) or an old transaction → no LARGE_TRANSACTION")
  void largeTransactionNeedsHistoryAndRecency() {
    List<ExpenseItem> fewPeers = new ArrayList<>(peers(FOOD, "400", 4, MONTH_START.minusDays(40)));
    fewPeers.add(new ExpenseItem(99L, FOOD, new BigDecimal("5000"), MONTH_START.plusDays(3), "big"));
    List<ExpenseItem> oldCandidate = new ArrayList<>(peers(FOOD, "400", 6, MONTH_START.minusDays(40)));
    oldCandidate.add(new ExpenseItem(98L, FOOD, new BigDecimal("5000"), MONTH_START.minusDays(5), "last month"));

    assertThat(AnomalyDetector.detect(List.of(), List.of(), fewPeers, MONTH_START)).isEmpty();
    assertThat(AnomalyDetector.detect(List.of(), List.of(), oldCandidate, MONTH_START)).isEmpty();
  }

  private static List<ExpenseItem> peers(Long categoryId, String amount, int count, LocalDate firstDate) {
    List<ExpenseItem> items = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      items.add(new ExpenseItem((long) (i + 1), categoryId, new BigDecimal(amount), firstDate.plusDays(i), "meal"));
    }
    return items;
  }
}
