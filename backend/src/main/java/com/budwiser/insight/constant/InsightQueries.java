package com.budwiser.insight.constant;

public final class InsightQueries {
  private InsightQueries() {}

  /**
   * Idempotent insert: the (user_id, dedupe_key) unique constraint turns a repeated fact into a no-op, which is
   * race-free across overlapping runs (unlike check-then-insert). Returns 1 when inserted, 0 when it already existed.
   */
  public static final String INSERT_IF_ABSENT = """
    INSERT INTO insights (user_id, type, severity, title, body, dedupe_key, created_at, modified_at, version)
    VALUES (:userId, :type, :severity, :title, :body, :dedupeKey, :now, :now, 0)
    ON CONFLICT (user_id, dedupe_key) DO NOTHING
    """;

  public static final String MARK_ALL_READ = """
    UPDATE insights
    SET read_at = :now, modified_at = :now, version = version + 1
    WHERE user_id = :userId
      AND read_at IS NULL
    """;

  public static final String DELETE_CREATED_BEFORE = """
    DELETE FROM insights
    WHERE created_at < :cutoff
    """;
}
