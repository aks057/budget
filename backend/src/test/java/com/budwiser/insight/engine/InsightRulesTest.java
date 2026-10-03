package com.budwiser.insight.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.budwiser.analytics.dto.AnomalyDto;
import com.budwiser.analytics.dto.RecurringExpenseDto;
import com.budwiser.analytics.engine.AnomalySeverity;
import com.budwiser.analytics.engine.AnomalyType;
import com.budwiser.analytics.engine.GoalCalculator;
import com.budwiser.insight.constant.InsightSeverity;
import com.budwiser.insight.constant.InsightType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InsightRulesTest {
  private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
  private static final YearMonth OCT_2026 = YearMonth.of(2026, 10);

  @Nested
  @DisplayName("anomalies")
  class Anomalies {

    @Test
    @DisplayName("budget over its limit → CRITICAL BUDGET_EXCEEDED, keyed per category and month")
    void budgetExceeded() {
      AnomalyDto anomaly = anomaly(AnomalyType.BUDGET_THRESHOLD, AnomalySeverity.CRITICAL, "1200", "1000", "120");

      InsightCandidate actual = single(facts(List.of(anomaly), List.of(), List.of(), null));

      assertThat(actual.type()).isEqualTo(InsightType.BUDGET_EXCEEDED);
      assertThat(actual.severity()).isEqualTo(InsightSeverity.CRITICAL);
      assertThat(actual.title()).isEqualTo("Food budget exceeded");
      assertThat(actual.body()).contains("₹1,200.00").contains("₹1,000.00").contains("120% used");
      assertThat(actual.dedupeKey()).isEqualTo("budget-exceeded:7:2026-10");
    }

    @Test
    @DisplayName("budget past the warning threshold → WARNING with the amount left this month")
    void budgetWarning() {
      AnomalyDto anomaly = anomaly(AnomalyType.BUDGET_THRESHOLD, AnomalySeverity.WARNING, "850", "1000", "85");

      InsightCandidate actual = single(facts(List.of(anomaly), List.of(), List.of(), null));

      assertThat(actual.type()).isEqualTo(InsightType.BUDGET_WARNING);
      assertThat(actual.title()).isEqualTo("Food budget at 85%");
      assertThat(actual.body()).contains("₹150.00 left for the rest of October");
      assertThat(actual.dedupeKey()).isEqualTo("budget-warning:7:2026-10");
    }

    @Test
    @DisplayName("spike keeps the engine's severity; large transaction is keyed by transaction")
    void spikeAndLargeTransaction() {
      AnomalyDto spike = anomaly(AnomalyType.SPENDING_SPIKE, AnomalySeverity.CRITICAL, "10500", "7000", "50");
      AnomalyDto large = new AnomalyDto(AnomalyType.LARGE_TRANSACTION, AnomalySeverity.WARNING, "7", "Food", "99",
        new BigDecimal("5000"), new BigDecimal("400"), new BigDecimal("1150"));

      List<InsightCandidate> actual = InsightRules.evaluate(facts(List.of(spike, large), List.of(), List.of(), null));

      assertThat(actual).extracting(InsightCandidate::type)
        .containsExactly(InsightType.SPENDING_SPIKE, InsightType.LARGE_TRANSACTION);
      assertThat(actual.get(0).severity()).isEqualTo(InsightSeverity.CRITICAL);
      assertThat(actual.get(0).title()).isEqualTo("Food spending is up 50%");
      assertThat(actual.get(1).dedupeKey()).isEqualTo("large-tx:99");
    }

    @Test
    @DisplayName("amounts use the user's currency")
    void userCurrency() {
      AnomalyDto anomaly = anomaly(AnomalyType.BUDGET_THRESHOLD, AnomalySeverity.CRITICAL, "1234.5", "1000", "123.5");

      InsightCandidate actual = single(new InsightFacts(TODAY, "USD", List.of(anomaly), List.of(), List.of(), null));

      assertThat(actual.body()).contains("$1,234.50").doesNotContain("₹");
    }
  }

  @Nested
  @DisplayName("recurring bills")
  class Bills {

    @Test
    @DisplayName("only bills expected today..today+3 are announced, with friendly timing")
    void dueWindow() {
      List<RecurringExpenseDto> bills = List.of(
        bill("Netflix", TODAY), bill("Gym", TODAY.plusDays(1)), bill("Rent", TODAY.plusDays(3)),
        bill("Insurance", TODAY.plusDays(4)), bill("Old", TODAY.minusDays(1)));

      List<InsightCandidate> actual = InsightRules.evaluate(facts(List.of(), bills, List.of(), null));

      assertThat(actual).extracting(InsightCandidate::title)
        .containsExactly("Netflix is due today", "Gym is due tomorrow", "Rent is due on 18 Oct");
      assertThat(actual.get(0).severity()).isEqualTo(InsightSeverity.INFO);
      assertThat(actual.get(0).dedupeKey()).isEqualTo("bill:3:netflix:2026-10-15");
    }
  }

  @Nested
  @DisplayName("goals")
  class Goals {

    @Test
    @DisplayName("achieved goal is celebrated once (key has no month), only right after it was reached")
    void achieved() {
      InsightCandidate actual = single(facts(List.of(), List.of(), List.of(goal(1L, "1000", "1000", 2027, 6)), null));

      assertThat(actual.type()).isEqualTo(InsightType.GOAL_ACHIEVED);
      assertThat(actual.dedupeKey()).isEqualTo("goal-achieved:1");

      // Reached long ago (e.g. after retention purged the original insight): not announced again.
      LocalDate targetDate = LocalDate.of(2027, 6, 28);
      var stale = new InsightFacts.GoalFact(1L, "Trip", targetDate, TODAY.minusDays(8),
        GoalCalculator.progress(new BigDecimal("1000"), new BigDecimal("1000"), targetDate, OCT_2026));
      assertThat(InsightRules.evaluate(facts(List.of(), List.of(), List.of(stale), null))).isEmpty();
    }

    @Test
    @DisplayName("past target date and not achieved → CRITICAL, reminded monthly")
    void overdue() {
      InsightCandidate actual = single(facts(List.of(), List.of(), List.of(goal(2L, "1000", "400", 2026, 8)), null));

      assertThat(actual.type()).isEqualTo(InsightType.GOAL_OVERDUE);
      assertThat(actual.severity()).isEqualTo(InsightSeverity.CRITICAL);
      assertThat(actual.body()).contains("₹600.00 still to save");
      assertThat(actual.dedupeKey()).isEqualTo("goal-overdue:2:2026-10");
    }

    @Test
    @DisplayName("behind pace: required monthly contribution exceeds recent average savings")
    void behind() {
      // 12,000 left over 12 months → 1,000/month needed; the user saves 600/month.
      InsightFacts facts = facts(List.of(), List.of(), List.of(goal(3L, "12000", "0", 2027, 10)), new BigDecimal("600"));

      InsightCandidate actual = single(facts);

      assertThat(actual.type()).isEqualTo(InsightType.GOAL_BEHIND);
      assertThat(actual.body()).contains("₹1,000.00/month").contains("₹600.00/month");
    }

    @Test
    @DisplayName("on pace, or no savings history to judge by → no insight")
    void onPaceOrNoHistory() {
      var goal = goal(3L, "12000", "0", 2027, 10);

      assertThat(InsightRules.evaluate(facts(List.of(), List.of(), List.of(goal), new BigDecimal("1500")))).isEmpty();
      assertThat(InsightRules.evaluate(facts(List.of(), List.of(), List.of(goal), null))).isEmpty();
    }
  }

  @Test
  @DisplayName("titles and keys are capped to the column sizes")
  void truncation() {
    String longName = "x".repeat(300);
    InsightCandidate actual = single(facts(List.of(), List.of(bill(longName, TODAY)), List.of(), null));

    assertThat(actual.title()).hasSize(120).endsWith("…");
    assertThat(actual.dedupeKey()).hasSize(200);
  }

  private static InsightCandidate single(InsightFacts facts) {
    List<InsightCandidate> candidates = InsightRules.evaluate(facts);
    assertThat(candidates).hasSize(1);
    return candidates.getFirst();
  }

  private static InsightFacts facts(List<AnomalyDto> anomalies, List<RecurringExpenseDto> bills,
                                    List<InsightFacts.GoalFact> goals, BigDecimal averageSavings) {
    return new InsightFacts(TODAY, "INR", anomalies, bills, goals, averageSavings);
  }

  private static AnomalyDto anomaly(AnomalyType type, AnomalySeverity severity, String actual, String baseline,
                                    String deviation) {
    return new AnomalyDto(type, severity, "7", "Food", null, new BigDecimal(actual), new BigDecimal(baseline),
      new BigDecimal(deviation));
  }

  private static RecurringExpenseDto bill(String description, LocalDate next) {
    return new RecurringExpenseDto(description, "3", "Bills", new BigDecimal("499"), 4, 30, next.minusDays(30), next);
  }

  private static InsightFacts.GoalFact goal(Long id, String target, String saved, int year, int month) {
    LocalDate targetDate = LocalDate.of(year, month, 28);
    return new InsightFacts.GoalFact(id, "Trip", targetDate, TODAY,
      GoalCalculator.progress(new BigDecimal(target), new BigDecimal(saved), targetDate, OCT_2026));
  }
}
