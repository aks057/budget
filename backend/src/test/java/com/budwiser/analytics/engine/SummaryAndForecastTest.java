package com.budwiser.analytics.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SummaryAndForecastTest {

  @Nested
  @DisplayName("SummaryCalculator")
  class Summary {

    @Test
    @DisplayName("PRD §2 example: income 90,000, expenses 36,500 → savings 53,500, rate 59.4%")
    void prdExample() {
      MonthlySummary actual = SummaryCalculator.summarize(new BigDecimal("90000"), new BigDecimal("36500"));

      assertThat(actual.savings()).isEqualByComparingTo("53500.00");
      assertThat(actual.savingsRate()).isEqualByComparingTo("59.4");
    }

    @Test
    @DisplayName("overspending gives negative savings and a negative rate")
    void deficit() {
      MonthlySummary actual = SummaryCalculator.summarize(new BigDecimal("40000"), new BigDecimal("50000"));

      assertThat(actual.savings()).isEqualByComparingTo("-10000.00");
      assertThat(actual.savingsRate()).isEqualByComparingTo("-25.0");
    }

    @Test
    @DisplayName("no income → savings rate is undefined (null), not 0%")
    void noIncome() {
      MonthlySummary actual = SummaryCalculator.summarize(null, new BigDecimal("1200"));

      assertThat(actual.income()).isEqualByComparingTo("0.00");
      assertThat(actual.savingsRate()).isNull();
    }
  }

  @Nested
  @DisplayName("ComparisonCalculator")
  class Comparison {

    @Test
    @DisplayName("PRD §20 example: 3-month average 7,000, current 10,500 → +50% deviation")
    void prdExample() {
      SpendingComparison actual = ComparisonCalculator.compare(new BigDecimal("10500"),
        List.of(new BigDecimal("8000"), new BigDecimal("7000"), new BigDecimal("6000")));

      assertThat(actual.threeMonthAverage()).isEqualByComparingTo("7000.00");
      assertThat(actual.deviationVsAveragePercent()).isEqualByComparingTo("50.0");
      assertThat(actual.previous()).isEqualByComparingTo("8000.00");
      assertThat(actual.changeVsPreviousPercent()).isEqualByComparingTo("31.3");
    }

    @Test
    @DisplayName("quiet months count as zero in the average; no previous spending → change is null")
    void missingMonths() {
      SpendingComparison actual = ComparisonCalculator.compare(new BigDecimal("900"),
        Arrays.asList(null, null, new BigDecimal("3000")));

      assertThat(actual.threeMonthAverage()).isEqualByComparingTo("1000.00");
      assertThat(actual.changeVsPreviousPercent()).isNull();
      assertThat(actual.deviationVsAveragePercent()).isEqualByComparingTo("-10.0");
    }

    @Test
    @DisplayName("requires exactly three prior months")
    void wrongWindow() {
      assertThatThrownBy(() -> ComparisonCalculator.compare(BigDecimal.ONE, List.of(BigDecimal.ONE)))
        .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("ForecastCalculator")
  class Forecast {

    @Test
    @DisplayName("₹6,000 in the first 10 of 30 days → ₹600/day → ₹18,000 projected")
    void runRate() {
      MonthForecast actual = ForecastCalculator.forecast(new BigDecimal("6000"), 10, 30);

      assertThat(actual.dailyRunRate()).isEqualByComparingTo("600.00");
      assertThat(actual.projectedTotal()).isEqualByComparingTo("18000.00");
    }

    @Test
    @DisplayName("projection is computed before rounding the daily rate (no compounding rounding error)")
    void noRoundingDrift() {
      MonthForecast actual = ForecastCalculator.forecast(new BigDecimal("1000"), 3, 31);

      assertThat(actual.dailyRunRate()).isEqualByComparingTo("333.33");
      assertThat(actual.projectedTotal()).isEqualByComparingTo("10333.33");
    }

    @Test
    @DisplayName("day zero projects only what is already spent; invalid day counts are rejected")
    void edges() {
      assertThat(ForecastCalculator.forecast(new BigDecimal("50"), 0, 30).projectedTotal()).isEqualByComparingTo("50.00");
      assertThatThrownBy(() -> ForecastCalculator.forecast(BigDecimal.ONE, 31, 30))
        .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
