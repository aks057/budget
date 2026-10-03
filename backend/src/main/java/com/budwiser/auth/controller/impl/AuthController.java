package com.budwiser.auth.controller.impl;

import com.budwiser.auth.controller.IAuthController;
import com.budwiser.auth.dto.AuthResponse;
import com.budwiser.auth.dto.LoginRequest;
import com.budwiser.auth.dto.RegisterRequest;
import com.budwiser.auth.model.AuthResult;
import com.budwiser.auth.service.IAuthService;
import com.budwiser.common.response.Response;
import com.budwiser.security.web.AuthCookieManager;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthController implements IAuthController {
  private static final String LOGGED_OUT_MESSAGE = "Logged out";

  private final IAuthService authService;
  private final AuthCookieManager authCookieManager;

  @Override
  public Response<AuthResponse> register(RegisterRequest request, String userAgent, HttpServletResponse httpResponse) {
    return withRefreshCookie(authService.register(request, userAgent), httpResponse);
  }

  @Override
  public Response<AuthResponse> login(LoginRequest request, String userAgent, HttpServletResponse httpResponse) {
    return withRefreshCookie(authService.login(request, userAgent), httpResponse);
  }

  @Override
  public Response<AuthResponse> refresh(String refreshToken, String userAgent, HttpServletResponse httpResponse) {
    return withRefreshCookie(authService.refresh(refreshToken, userAgent), httpResponse);
  }

  @Override
  public Response<Void> logout(String refreshToken, HttpServletResponse httpResponse) {
    authService.logout(refreshToken);
    authCookieManager.clearRefreshCookie(httpResponse);
    return Response.<Void>builder().message(LOGGED_OUT_MESSAGE).build();
  }

  private Response<AuthResponse> withRefreshCookie(AuthResult result, HttpServletResponse httpResponse) {
    authCookieManager.writeRefreshCookie(httpResponse, result.refreshToken());
    return Response.<AuthResponse>builder().data(result.response()).build();
  }
}
