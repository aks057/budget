package com.budwiser.analytics.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BudgetCalculatorTest {

  @Test
  @DisplayName("PRD example: 8,400 of 10,000 spent → 84% used, 1,600 remaining, WARNING")
  void prdShoppingExample() {
    // Act
    BudgetStatus actual = BudgetCalculator.calculate(new BigDecimal("10000"), new BigDecimal("8400"));

    // Assert
    assertThat(actual.spent()).isEqualByComparingTo("8400.00");
    assertThat(actual.remaining()).isEqualByComparingTo("1600.00");
    assertThat(actual.percentUsed()).isEqualByComparingTo("84.0");
    assertThat(actual.level()).isEqualTo(BudgetLevel.WARNING);
  }

  @ParameterizedTest(name = "spent {0} of 1000 → {1}")
  @CsvSource({
    "0, ON_TRACK",
    "799.49, ON_TRACK",
    "799.50, WARNING",
    "800, WARNING",
    "999.99, EXCEEDED",
    "1000, EXCEEDED",
    "2500, EXCEEDED"
  })
  @DisplayName("level thresholds: WARNING from 80%, EXCEEDED from 100% (percent rounded to 1 decimal)")
  void levelThresholds(String spent, BudgetLevel expectedLevel) {
    BudgetStatus actual = BudgetCalculator.calculate(new BigDecimal("1000"), new BigDecimal(spent));

    assertThat(actual.level()).isEqualTo(expectedLevel);
  }

  @Test
  @DisplayName("overspending yields a negative remaining amount")
  void overspent() {
    BudgetStatus actual = BudgetCalculator.calculate(new BigDecimal("5000"), new BigDecimal("6250.50"));

    assertThat(actual.remaining()).isEqualByComparingTo("-1250.50");
    assertThat(actual.percentUsed()).isEqualByComparingTo("125.0");
  }

  @Test
  @DisplayName("no spending (null total from the query) is treated as zero")
  void nullSpent() {
    BudgetStatus actual = BudgetCalculator.calculate(new BigDecimal("3000"), null);

    assertThat(actual.spent()).isEqualByComparingTo("0.00");
    assertThat(actual.remaining()).isEqualByComparingTo("3000.00");
    assertThat(actual.percentUsed()).isEqualByComparingTo("0.0");
    assertThat(actual.level()).isEqualTo(BudgetLevel.ON_TRACK);
  }
}
