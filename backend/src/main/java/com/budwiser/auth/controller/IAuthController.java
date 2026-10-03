package com.budwiser.auth.controller;

import com.budwiser.auth.constant.AuthConstants;
import com.budwiser.auth.dto.AuthResponse;
import com.budwiser.auth.dto.LoginRequest;
import com.budwiser.auth.dto.RegisterRequest;
import com.budwiser.common.ratelimit.RateLimit;
import com.budwiser.common.ratelimit.RateLimitPolicy;
import com.budwiser.common.response.Response;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public interface IAuthController {

  @RateLimit(RateLimitPolicy.AUTH)
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  Response<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                  @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                                  HttpServletResponse httpResponse);

  @RateLimit(RateLimitPolicy.AUTH)
  @PostMapping("/login")
  Response<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                               @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                               HttpServletResponse httpResponse);

  @RateLimit(RateLimitPolicy.AUTH)
  @PostMapping("/refresh")
  Response<AuthResponse> refresh(@CookieValue(value = AuthConstants.REFRESH_COOKIE_NAME, required = false) String refreshToken,
                                 @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                                 HttpServletResponse httpResponse);

  @PostMapping("/logout")
  Response<Void> logout(@CookieValue(value = AuthConstants.REFRESH_COOKIE_NAME, required = false) String refreshToken,
                        HttpServletResponse httpResponse);
}
