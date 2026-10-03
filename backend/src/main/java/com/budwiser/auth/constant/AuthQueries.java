package com.budwiser.auth.constant;

public final class AuthQueries {
  private AuthQueries() {}

  public static final String REVOKE_REFRESH_TOKEN_FAMILY = """
    UPDATE refresh_tokens
    SET revoked_at = :now, modified_at = :now, version = version + 1
    WHERE family_id = :familyId
      AND revoked_at IS NULL
    """;
}
