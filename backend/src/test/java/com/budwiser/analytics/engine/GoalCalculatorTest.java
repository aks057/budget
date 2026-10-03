package com.budwiser.analytics.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoalCalculatorTest {
  private static final YearMonth DEC_2026 = YearMonth.of(2026, 12);

  @Test
  @DisplayName("PRD example: 3,00,000 target, 1,20,000 saved, Dec 2027 deadline → 15,000/month for 12 months")
  void prdEmergencyFundExample() {
    // Act
    GoalProgress actual = GoalCalculator.progress(new BigDecimal("300000"), new BigDecimal("120000"),
      LocalDate.of(2027, 12, 31), DEC_2026);

    // Assert
    assertThat(actual.remaining()).isEqualByComparingTo("180000.00");
    assertThat(actual.monthsRemaining()).isEqualTo(12);
    assertThat(actual.requiredMonthlyContribution()).isEqualByComparingTo("15000.00");
    assertThat(actual.percentComplete()).isEqualByComparingTo("40.0");
    assertThat(actual.achieved()).isFalse();
    assertThat(actual.overdue()).isFalse();
  }

  @Test
  @DisplayName("required contribution rounds UP to the paisa so the goal is actually reached")
  void roundsUp() {
    GoalProgress actual = GoalCalculator.progress(new BigDecimal("100"), BigDecimal.ZERO,
      LocalDate.of(2027, 3, 1), DEC_2026);

    assertThat(actual.monthsRemaining()).isEqualTo(3);
    assertThat(actual.requiredMonthlyContribution()).isEqualByComparingTo("33.34");
  }

  @Test
  @DisplayName("goal due this month needs the whole remainder now")
  void dueThisMonth() {
    GoalProgress actual = GoalCalculator.progress(new BigDecimal("50000"), new BigDecimal("20000"),
      LocalDate.of(2026, 12, 20), DEC_2026);

    assertThat(actual.monthsRemaining()).isZero();
    assertThat(actual.requiredMonthlyContribution()).isEqualByComparingTo("30000.00");
    assertThat(actual.overdue()).isFalse();
  }

  @Test
  @DisplayName("past deadline and not achieved → overdue, whole remainder required")
  void overdue() {
    GoalProgress actual = GoalCalculator.progress(new BigDecimal("50000"), new BigDecimal("20000"),
      LocalDate.of(2026, 6, 30), DEC_2026);

    assertThat(actual.overdue()).isTrue();
    assertThat(actual.monthsRemaining()).isZero();
    assertThat(actual.requiredMonthlyContribution()).isEqualByComparingTo("30000.00");
  }

  @Test
  @DisplayName("saving more than the target → achieved, nothing remaining, percent capped at 100")
  void achievedAndCapped() {
    GoalProgress actual = GoalCalculator.progress(new BigDecimal("10000"), new BigDecimal("12500"),
      LocalDate.of(2027, 6, 30), DEC_2026);

    assertThat(actual.achieved()).isTrue();
    assertThat(actual.remaining()).isEqualByComparingTo("0.00");
    assertThat(actual.requiredMonthlyContribution()).isEqualByComparingTo("0.00");
    assertThat(actual.percentComplete()).isEqualByComparingTo("100.0");
  }
}
