package com.budwiser.transaction.constant;

public final class TransactionQueries {
  private TransactionQueries() {}

  /*
   * Optional filters use "CAST(:p AS type) IS NULL OR column = :p": Postgres cannot infer the type of a bare
   * NULL bind parameter, the cast gives it one. Both date bounds are always bound (service applies defaults).
   */
  private static final String SEARCH_WHERE = """
    WHERE t.user_id = :userId
      AND t.transaction_date >= :from
      AND t.transaction_date <= :to
      AND (CAST(:type AS VARCHAR) IS NULL OR t.type = CAST(:type AS VARCHAR))
      AND (CAST(:categoryId AS BIGINT) IS NULL OR t.category_id = CAST(:categoryId AS BIGINT))
    """;

  public static final String SEARCH = """
    SELECT t.id, t.user_id, t.category_id, t.type, t.amount, t.description, t.transaction_date,
           t.created_at, t.modified_at, t.version
    FROM transactions t
    """ + SEARCH_WHERE + """
    ORDER BY t.transaction_date DESC, t.id DESC
    """;

  public static final String COUNT_SEARCH = """
    SELECT COUNT(1)
    FROM transactions t
    """ + SEARCH_WHERE;
}
