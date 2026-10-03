package com.budwiser.analytics.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecurringExpenseDetectorTest {
  private static final Long BILLS = 5L;

  @Test
  @DisplayName("monthly Netflix with noisy descriptions is detected; next date = last + median interval")
  void detectsMonthlySubscription() {
    List<ExpenseItem> expenses = List.of(
      item(1, "649", LocalDate.of(2026, 7, 5), "NETFLIX #8812"),
      item(2, "649", LocalDate.of(2026, 8, 5), "Netflix"),
      item(3, "649", LocalDate.of(2026, 9, 4), "netflix 9921"),
      item(4, "120", LocalDate.of(2026, 9, 10), "Chai"));

    List<RecurringExpense> actual = RecurringExpenseDetector.detect(expenses);

    assertThat(actual).singleElement().satisfies(recurring -> {
      assertThat(recurring.description()).isEqualTo("netflix 9921");
      assertThat(recurring.typicalAmount()).isEqualByComparingTo("649.00");
      assertThat(recurring.occurrences()).isEqualTo(3);
      assertThat(recurring.intervalDays()).isBetween(30, 31);
      assertThat(recurring.nextExpectedDate()).isEqualTo(recurring.lastDate().plusDays(recurring.intervalDays()));
    });
  }

  @Test
  @DisplayName("small price changes (±20%) still count; a big jump does not")
  void amountTolerance() {
    List<ExpenseItem> priceBump = List.of(
      item(1, "1000", LocalDate.of(2026, 6, 1), "Broadband"),
      item(2, "1000", LocalDate.of(2026, 7, 1), "Broadband"),
      item(3, "1150", LocalDate.of(2026, 8, 1), "Broadband"));
    List<ExpenseItem> erratic = List.of(
      item(1, "300", LocalDate.of(2026, 6, 1), "Amazon"),
      item(2, "3000", LocalDate.of(2026, 7, 1), "Amazon"),
      item(3, "900", LocalDate.of(2026, 8, 1), "Amazon"));

    assertThat(RecurringExpenseDetector.detect(priceBump)).hasSize(1);
    assertThat(RecurringExpenseDetector.detect(erratic)).isEmpty();
  }

  @Test
  @DisplayName("weekly or irregular spending and fewer than 3 months are not 'recurring monthly'")
  void rejectsNonMonthly() {
    List<ExpenseItem> weekly = List.of(
      item(1, "200", LocalDate.of(2026, 9, 1), "Gym class"),
      item(2, "200", LocalDate.of(2026, 9, 8), "Gym class"),
      item(3, "200", LocalDate.of(2026, 9, 15), "Gym class"),
      item(4, "200", LocalDate.of(2026, 9, 22), "Gym class"));
    List<ExpenseItem> twoMonths = List.of(
      item(1, "499", LocalDate.of(2026, 8, 3), "Spotify"),
      item(2, "499", LocalDate.of(2026, 9, 3), "Spotify"));

    assertThat(RecurringExpenseDetector.detect(weekly)).isEmpty();
    assertThat(RecurringExpenseDetector.detect(twoMonths)).isEmpty();
  }

  @Test
  @DisplayName("normalization keeps letters only, lower-cased, single-spaced")
  void normalize() {
    assertThat(RecurringExpenseDetector.normalize("  JIO-Fiber  #123 (Oct) ")).isEqualTo("jio fiber oct");
    assertThat(RecurringExpenseDetector.normalize("1234")).isEmpty();
    assertThat(RecurringExpenseDetector.normalize(null)).isEmpty();
  }

  private static ExpenseItem item(long id, String amount, LocalDate date, String description) {
    return new ExpenseItem(id, BILLS, new BigDecimal(amount), date, description);
  }
}
