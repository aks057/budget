package com.budwiser.security.oauth;

import com.budwiser.auth.constant.AuthConstants;
import com.budwiser.common.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
  private final AppProperties appProperties;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException {
    log.warn("[onAuthenticationFailure] google login failed: {}", exception.getClass().getSimpleName());
    response.sendRedirect(appProperties.frontendUrl() + AuthConstants.FRONTEND_OAUTH_FAILURE_PATH);
  }
}
