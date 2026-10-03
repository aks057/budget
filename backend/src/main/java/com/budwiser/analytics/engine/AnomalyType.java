package com.budwiser.analytics.engine;

public enum AnomalyType {
  /** Category spending this month is well above its 3-month average. */
  SPENDING_SPIKE,
  /** A budget reached the warning threshold or was exceeded. */
  BUDGET_THRESHOLD,
  /** A single expense far larger than usual for its category. */
  LARGE_TRANSACTION
}
