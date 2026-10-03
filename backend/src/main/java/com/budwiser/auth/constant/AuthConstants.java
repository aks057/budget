package com.budwiser.auth.constant;

public final class AuthConstants {
  private AuthConstants() {}

  public static final String REFRESH_COOKIE_NAME = "bw_refresh";
  /** Cookie is only ever sent to the auth endpoints, never to regular API calls. */
  public static final String REFRESH_COOKIE_PATH = "/api/v1/auth";
  public static final String TOKEN_TYPE_BEARER = "Bearer";
  public static final String SCOPE_USER = "user";
  public static final String CLAIM_EMAIL = "email";
  public static final String CLAIM_SCOPE = "scope";
  public static final String DEFAULT_CURRENCY = "INR";
  public static final int USER_AGENT_MAX_LENGTH = 255;

  /** Frontend routes the backend redirects to after Google sign-in. */
  public static final String FRONTEND_OAUTH_CALLBACK_PATH = "/auth/callback";
  public static final String FRONTEND_OAUTH_FAILURE_PATH = "/sign-in?error=oauth_failed";
}
