package com.budwiser.analytics.engine;

import com.budwiser.common.util.MoneyMath;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Finds monthly subscriptions/bills: same category + same normalized description, at least
 * {@link #MIN_OCCURRENCES} times in distinct months, roughly monthly gaps and a stable amount.
 */
public final class RecurringExpenseDetector {
  private RecurringExpenseDetector() {}

  public static final int MIN_OCCURRENCES = 3;
  public static final int MIN_INTERVAL_DAYS = 25;
  public static final int MAX_INTERVAL_DAYS = 35;
  /** Every occurrence must be within ±20% of the median amount (price changes and taxes vary a little). */
  public static final BigDecimal AMOUNT_TOLERANCE_PERCENT = BigDecimal.valueOf(20);

  public static List<RecurringExpense> detect(List<ExpenseItem> expenses) {
    Map<String, List<ExpenseItem>> groups = expenses.stream()
      .filter(item -> !normalize(item.description()).isEmpty())
      .collect(Collectors.groupingBy(item -> item.categoryId() + "|" + normalize(item.description())));

    List<RecurringExpense> recurring = new ArrayList<>();
    for (List<ExpenseItem> group : groups.values()) {
      evaluate(group).ifPresent(recurring::add);
    }
    recurring.sort(Comparator.comparing(RecurringExpense::typicalAmount).reversed());
    return recurring;
  }

  private static Optional<RecurringExpense> evaluate(List<ExpenseItem> group) {
    if (group.size() < MIN_OCCURRENCES) {
      return Optional.empty();
    }
    List<ExpenseItem> sorted = group.stream().sorted(Comparator.comparing(ExpenseItem::date)).toList();
    long distinctMonths = sorted.stream().map(item -> YearMonth.from(item.date())).distinct().count();
    if (distinctMonths < MIN_OCCURRENCES) {
      return Optional.empty();
    }

    List<BigDecimal> intervals = new ArrayList<>();
    for (int i = 1; i < sorted.size(); i++) {
      intervals.add(BigDecimal.valueOf(ChronoUnit.DAYS.between(sorted.get(i - 1).date(), sorted.get(i).date())));
    }
    int medianInterval = MoneyMath.median(intervals).intValue();
    if (medianInterval < MIN_INTERVAL_DAYS || medianInterval > MAX_INTERVAL_DAYS) {
      return Optional.empty();
    }

    BigDecimal typicalAmount = MoneyMath.median(sorted.stream().map(ExpenseItem::amount).toList());
    boolean stableAmount = sorted.stream().allMatch(item -> {
      BigDecimal deviation = MoneyMath.changePercent(item.amount(), typicalAmount);
      return deviation != null && deviation.abs().compareTo(AMOUNT_TOLERANCE_PERCENT) <= 0;
    });
    if (!stableAmount) {
      return Optional.empty();
    }

    ExpenseItem last = sorted.getLast();
    return Optional.of(new RecurringExpense(last.description(), last.categoryId(), typicalAmount, sorted.size(),
      medianInterval, last.date(), last.date().plusDays(medianInterval)));
  }

  /** "Netflix #4521" and "NETFLIX" are the same merchant: lower-case, letters only, single spaces. */
  static String normalize(String description) {
    if (description == null) {
      return "";
    }
    return description.toLowerCase(Locale.ROOT)
      .replaceAll("[^\\p{L} ]", " ")
      .replaceAll("\\s+", " ")
      .trim();
  }
}
