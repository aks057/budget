package com.budwiser.analytics.engine;

import com.budwiser.common.util.MoneyMath;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Pure savings-goal math. Example (PRD §11): target 3,00,000, saved 1,20,000, deadline Dec 2027, today Dec 2026
 * → remaining 1,80,000 over 12 months → 15,000/month.
 */
public final class GoalCalculator {
  private GoalCalculator() {}

  public static GoalProgress progress(BigDecimal target, BigDecimal current, LocalDate targetDate, YearMonth currentMonth) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(targetDate, "targetDate");
    Objects.requireNonNull(currentMonth, "currentMonth");
    BigDecimal saved = current == null ? BigDecimal.ZERO : current;

    BigDecimal remaining = target.subtract(saved).max(BigDecimal.ZERO);
    BigDecimal percentComplete = MoneyMath.percent(saved, target).min(MoneyMath.HUNDRED.setScale(MoneyMath.PERCENT_SCALE));
    boolean achieved = remaining.signum() == 0;
    long monthsRemaining = Math.max(ChronoUnit.MONTHS.between(currentMonth, YearMonth.from(targetDate)), 0);
    boolean overdue = !achieved && YearMonth.from(targetDate).isBefore(currentMonth);

    // Due this month or overdue: the whole remainder is needed now.
    BigDecimal required = achieved
      ? BigDecimal.ZERO
      : remaining.divide(BigDecimal.valueOf(Math.max(monthsRemaining, 1)), MoneyMath.MONEY_SCALE, RoundingMode.CEILING);

    return new GoalProgress(MoneyMath.money(remaining), percentComplete, monthsRemaining,
      MoneyMath.money(required), achieved, overdue);
  }
}
