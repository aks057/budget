package com.budwiser.security.oauth;

import com.budwiser.auth.constant.AuthConstants;
import com.budwiser.auth.model.AuthResult;
import com.budwiser.auth.model.GoogleProfile;
import com.budwiser.auth.service.IAuthService;
import com.budwiser.common.config.AppProperties;
import com.budwiser.common.exception.AuthException;
import com.budwiser.security.web.AuthCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * After Google OIDC login: find/link/create the user, set OUR refresh cookie, and send the browser to the frontend.
 * The frontend then calls /api/v1/auth/refresh to get an access token — no token ever travels in a URL.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
  private final IAuthService authService;
  private final AuthCookieManager authCookieManager;
  private final AppProperties appProperties;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
    invalidateHandshakeSession(request);
    if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
      log.warn("[onAuthenticationSuccess] unexpected principal type, rejecting login");
      response.sendRedirect(appProperties.frontendUrl() + AuthConstants.FRONTEND_OAUTH_FAILURE_PATH);
      return;
    }
    GoogleProfile profile = new GoogleProfile(
      oidcUser.getSubject(),
      oidcUser.getEmail(),
      Boolean.TRUE.equals(oidcUser.getEmailVerified()),
      oidcUser.getFullName(),
      oidcUser.getPicture());
    try {
      AuthResult result = authService.loginWithGoogle(profile, request.getHeader(HttpHeaders.USER_AGENT));
      authCookieManager.writeRefreshCookie(response, result.refreshToken());
      response.sendRedirect(appProperties.frontendUrl() + AuthConstants.FRONTEND_OAUTH_CALLBACK_PATH);
    } catch (AuthException ex) {
      // Boundary classification: a business rejection (e.g. unverified email) becomes a redirect, not a 500.
      log.warn("[onAuthenticationSuccess] google login rejected: {}", ex.getMessage());
      response.sendRedirect(appProperties.frontendUrl() + AuthConstants.FRONTEND_OAUTH_FAILURE_PATH);
    }
  }

  /** The servlet session only exists to hold the OAuth2 state/nonce during the redirect round-trip. */
  private static void invalidateHandshakeSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
  }
}
