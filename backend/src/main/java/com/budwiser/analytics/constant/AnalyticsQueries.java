package com.budwiser.analytics.constant;

/**
 * Aggregations over transactions. Every query filters on (user_id, transaction_date) — served by
 * idx_transactions_user_date — and uses a half-open range [from, toExclusive).
 * Aliases are quoted to keep camelCase for the projections; EXTRACT results are cast to INTEGER so projections
 * receive plain Integers rather than driver-specific numeric/date types.
 */
public final class AnalyticsQueries {
  private AnalyticsQueries() {}

  private static final String USER_AND_RANGE = """
    WHERE t.user_id = :userId
      AND t.transaction_date >= :from
      AND t.transaction_date < :toExclusive
    """;

  public static final String TOTALS_BY_TYPE = """
    SELECT t.type AS "type", SUM(t.amount) AS "total"
    FROM transactions t
    """ + USER_AND_RANGE + """
    GROUP BY t.type
    """;

  public static final String TOTALS_BY_CATEGORY = """
    SELECT t.category_id AS "categoryId", SUM(t.amount) AS "total"
    FROM transactions t
    """ + USER_AND_RANGE + """
      AND t.type = :type
    GROUP BY t.category_id
    """;

  public static final String MONTHLY_TOTALS_BY_TYPE = """
    SELECT CAST(EXTRACT(YEAR FROM t.transaction_date) AS INTEGER)  AS "year",
           CAST(EXTRACT(MONTH FROM t.transaction_date) AS INTEGER) AS "month",
           t.type AS "type",
           SUM(t.amount) AS "total"
    FROM transactions t
    """ + USER_AND_RANGE + """
    GROUP BY 1, 2, 3
    """;

  public static final String MONTHLY_EXPENSE_BY_CATEGORY = """
    SELECT CAST(EXTRACT(YEAR FROM t.transaction_date) AS INTEGER)  AS "year",
           CAST(EXTRACT(MONTH FROM t.transaction_date) AS INTEGER) AS "month",
           t.category_id AS "categoryId",
           SUM(t.amount) AS "total"
    FROM transactions t
    """ + USER_AND_RANGE + """
      AND t.type = 'EXPENSE'
    GROUP BY 1, 2, 3
    """;

  public static final String DAILY_TOTALS_BY_TYPE = """
    SELECT CAST(EXTRACT(DAY FROM t.transaction_date) AS INTEGER) AS "day",
           t.type AS "type",
           SUM(t.amount) AS "total"
    FROM transactions t
    """ + USER_AND_RANGE + """
    GROUP BY 1, 2
    """;

  public static final String DISTINCT_YEARS = """
    SELECT DISTINCT CAST(EXTRACT(YEAR FROM t.transaction_date) AS INTEGER) AS "year"
    FROM transactions t
    WHERE t.user_id = :userId
    ORDER BY 1
    """;

  /** Bounded by the caller's window (at most ~6 months of one user's expenses). */
  public static final String EXPENSES_IN_RANGE = """
    SELECT t.id, t.user_id, t.category_id, t.type, t.amount, t.description, t.transaction_date,
           t.created_at, t.modified_at, t.version
    FROM transactions t
    """ + USER_AND_RANGE + """
      AND t.type = 'EXPENSE'
    ORDER BY t.transaction_date, t.id
    """;
}
