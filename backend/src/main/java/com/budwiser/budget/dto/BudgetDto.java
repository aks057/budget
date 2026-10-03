package com.budwiser.budget.dto;

import com.budwiser.analytics.engine.BudgetLevel;
import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.Builder;
import lombok.Getter;

/**
 * A budget plus its status for {@link #month}. remaining is negative when overspent.
 */
@Getter
@Builder
public class BudgetDto {
  private final String id;
  private final String categoryId;
  private final String categoryName;
  private final String categoryIcon;
  private final BigDecimal amount;
  private final YearMonth month;
  private final BigDecimal spent;
  private final BigDecimal remaining;
  private final BigDecimal percentUsed;
  private final BudgetLevel level;
}
