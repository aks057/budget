package com.budwiser.insight.constant;

public final class InsightConstants {
  private InsightConstants() {}

  /** Most recent insights returned by the list endpoint. */
  public static final int MAX_INSIGHTS_LISTED = 50;

  /** Insights older than this are purged by the daily job. */
  public static final int RETENTION_DAYS = 90;

  /** Users processed per keyset page by the daily job. */
  public static final int USER_BATCH_SIZE = 200;

  /** A recurring bill is announced when it is expected within this many days. */
  public static final int BILL_DUE_WINDOW_DAYS = 3;

  /** An achieved goal is announced only if it was updated this recently (so retention never re-triggers it). */
  public static final int GOAL_ACHIEVED_WINDOW_DAYS = 7;

  /** Full months of history used to estimate the user's monthly savings pace. */
  public static final int SAVINGS_PACE_MONTHS = 3;

  public static final int TITLE_MAX_LENGTH = 120;
  public static final int BODY_MAX_LENGTH = 500;
  public static final int DEDUPE_KEY_MAX_LENGTH = 200;
}
