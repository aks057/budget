package com.budwiser.analytics.engine;

import com.budwiser.common.util.MoneyMath;
import java.math.BigDecimal;
import java.util.List;

/**
 * PRD §12 historical comparison: current month vs previous month vs the average of the 3 months before it.
 * Months with no spending count as zero in the average (a quiet month is real data, not missing data).
 */
public final class ComparisonCalculator {
  private ComparisonCalculator() {}

  public static final int AVERAGE_WINDOW_MONTHS = 3;

  /**
   * @param priorMonths totals of the {@link #AVERAGE_WINDOW_MONTHS} months before the current one, most recent first
   */
  public static SpendingComparison compare(BigDecimal current, List<BigDecimal> priorMonths) {
    if (priorMonths.size() != AVERAGE_WINDOW_MONTHS) {
      throw new IllegalArgumentException("Expected " + AVERAGE_WINDOW_MONTHS + " prior months, got " + priorMonths.size());
    }
    BigDecimal actualCurrent = MoneyMath.zeroIfNull(current);
    BigDecimal previous = MoneyMath.zeroIfNull(priorMonths.getFirst());
    BigDecimal total = priorMonths.stream().map(MoneyMath::zeroIfNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal average = MoneyMath.average(total, AVERAGE_WINDOW_MONTHS);
    return new SpendingComparison(
      MoneyMath.money(actualCurrent),
      MoneyMath.money(previous),
      average,
      MoneyMath.changePercent(actualCurrent, previous),
      MoneyMath.changePercent(actualCurrent, average));
  }
}
