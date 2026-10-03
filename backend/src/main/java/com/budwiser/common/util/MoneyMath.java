package com.budwiser.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Single place for money/percentage rounding rules so every module (and the agent) reports identical numbers.
 */
public final class MoneyMath {
  private MoneyMath() {}

  public static final int MONEY_SCALE = 2;
  public static final int PERCENT_SCALE = 1;
  public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final BigDecimal TWO = BigDecimal.valueOf(2);

  public static BigDecimal money(BigDecimal value) {
    return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }

  public static BigDecimal zeroIfNull(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  /** part / whole × 100, rounded to one decimal; zero when whole is zero (no division by zero). */
  public static BigDecimal percent(BigDecimal part, BigDecimal whole) {
    if (whole.signum() == 0) {
      return BigDecimal.ZERO.setScale(PERCENT_SCALE, RoundingMode.HALF_UP);
    }
    return part.multiply(HUNDRED).divide(whole, PERCENT_SCALE, RoundingMode.HALF_UP);
  }

  /** (current − baseline) / baseline × 100; null when there is no baseline (a change from zero is undefined). */
  public static BigDecimal changePercent(BigDecimal current, BigDecimal baseline) {
    if (baseline == null || baseline.signum() == 0) {
      return null;
    }
    return current.subtract(baseline).multiply(HUNDRED).divide(baseline, PERCENT_SCALE, RoundingMode.HALF_UP);
  }

  public static BigDecimal average(BigDecimal total, int count) {
    return total.divide(BigDecimal.valueOf(count), MONEY_SCALE, RoundingMode.HALF_UP);
  }

  /** Median of a non-empty list (any order). */
  public static BigDecimal median(List<BigDecimal> values) {
    List<BigDecimal> sorted = values.stream().sorted().toList();
    int middle = sorted.size() / 2;
    BigDecimal median = sorted.size() % 2 == 1
      ? sorted.get(middle)
      : sorted.get(middle - 1).add(sorted.get(middle)).divide(TWO, MONEY_SCALE, RoundingMode.HALF_UP);
    return money(median);
  }
}
