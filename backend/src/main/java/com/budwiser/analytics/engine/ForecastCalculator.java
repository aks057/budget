package com.budwiser.analytics.engine;

import com.budwiser.common.util.MoneyMath;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Run-rate forecast: spending so far ÷ days elapsed × days in month. Deliberately simple and explainable;
 * it is the baseline any smarter model would have to beat.
 */
public final class ForecastCalculator {
  private ForecastCalculator() {}

  public static MonthForecast forecast(BigDecimal spentSoFar, int daysElapsed, int daysInMonth) {
    if (daysInMonth <= 0 || daysElapsed < 0 || daysElapsed > daysInMonth) {
      throw new IllegalArgumentException("Invalid day counts: elapsed=" + daysElapsed + ", inMonth=" + daysInMonth);
    }
    BigDecimal spent = MoneyMath.money(MoneyMath.zeroIfNull(spentSoFar));
    if (daysElapsed == 0) {
      return new MonthForecast(spent, MoneyMath.money(BigDecimal.ZERO), spent, 0, daysInMonth);
    }
    BigDecimal dailyRunRate = spent.divide(BigDecimal.valueOf(daysElapsed), MoneyMath.MONEY_SCALE, RoundingMode.HALF_UP);
    BigDecimal projected = spent.multiply(BigDecimal.valueOf(daysInMonth))
      .divide(BigDecimal.valueOf(daysElapsed), MoneyMath.MONEY_SCALE, RoundingMode.HALF_UP);
    return new MonthForecast(spent, dailyRunRate, projected, daysElapsed, daysInMonth);
  }
}
