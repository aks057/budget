package com.budwiser.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Body returned on register/login/refresh. The refresh token is never in the body — only in the httpOnly cookie.
 */
@Getter
@Builder
public class AuthResponse {
  private final String accessToken;
  private final String tokenType;
  private final long expiresIn;
  private final UserDto user;
}
