package com.budwiser.analytics.engine;

import com.budwiser.common.util.MoneyMath;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Pure budget math (no Spring, no I/O). The agent never computes these numbers itself — it reads them from here.
 */
public final class BudgetCalculator {
  private BudgetCalculator() {}

  public static final BigDecimal WARNING_THRESHOLD_PERCENT = BigDecimal.valueOf(80);
  public static final BigDecimal EXCEEDED_THRESHOLD_PERCENT = BigDecimal.valueOf(100);

  public static BudgetStatus calculate(BigDecimal limit, BigDecimal spent) {
    Objects.requireNonNull(limit, "limit");
    BigDecimal actualSpent = spent == null ? BigDecimal.ZERO : spent;
    BigDecimal percentUsed = MoneyMath.percent(actualSpent, limit);
    return new BudgetStatus(
      MoneyMath.money(limit),
      MoneyMath.money(actualSpent),
      MoneyMath.money(limit.subtract(actualSpent)),
      percentUsed,
      levelFor(percentUsed));
  }

  private static BudgetLevel levelFor(BigDecimal percentUsed) {
    if (percentUsed.compareTo(EXCEEDED_THRESHOLD_PERCENT) >= 0) {
      return BudgetLevel.EXCEEDED;
    }
    if (percentUsed.compareTo(WARNING_THRESHOLD_PERCENT) >= 0) {
      return BudgetLevel.WARNING;
    }
    return BudgetLevel.ON_TRACK;
  }
}
