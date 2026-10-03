package com.budwiser.analytics.engine;

import com.budwiser.common.util.MoneyMath;
import java.math.BigDecimal;

/**
 * PRD §12: savings = income − expenses; savings rate = savings / income × 100.
 */
public final class SummaryCalculator {
  private SummaryCalculator() {}

  public static MonthlySummary summarize(BigDecimal income, BigDecimal expense) {
    BigDecimal totalIncome = MoneyMath.zeroIfNull(income);
    BigDecimal totalExpense = MoneyMath.zeroIfNull(expense);
    BigDecimal savings = totalIncome.subtract(totalExpense);
    BigDecimal savingsRate = totalIncome.signum() == 0 ? null : MoneyMath.percent(savings, totalIncome);
    return new MonthlySummary(MoneyMath.money(totalIncome), MoneyMath.money(totalExpense), MoneyMath.money(savings),
      savingsRate);
  }
}
