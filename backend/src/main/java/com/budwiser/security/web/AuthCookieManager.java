package com.budwiser.security.web;

import com.budwiser.auth.constant.AuthConstants;
import com.budwiser.auth.model.IssuedRefreshToken;
import com.budwiser.security.properties.AuthCookieProperties;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Writes the refresh-token cookie: httpOnly (no JS access), SameSite=Strict (no cross-site sends, so no CSRF),
 * Secure in production, and scoped to the auth endpoints only.
 */
@Component
@RequiredArgsConstructor
public class AuthCookieManager {
  private static final String SAME_SITE_STRICT = "Strict";

  private final AuthCookieProperties cookieProperties;

  public void writeRefreshCookie(HttpServletResponse response, IssuedRefreshToken refreshToken) {
    addCookie(response, refreshToken.rawToken(), refreshToken.timeToLive());
  }

  public void clearRefreshCookie(HttpServletResponse response) {
    addCookie(response, "", Duration.ZERO);
  }

  private void addCookie(HttpServletResponse response, String value, Duration maxAge) {
    ResponseCookie cookie = ResponseCookie.from(AuthConstants.REFRESH_COOKIE_NAME, value)
      .httpOnly(true)
      .secure(cookieProperties.secure())
      .sameSite(SAME_SITE_STRICT)
      .path(AuthConstants.REFRESH_COOKIE_PATH)
      .maxAge(maxAge)
      .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
