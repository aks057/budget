package com.budwiser.security.service.impl;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.AuthException;
import com.budwiser.security.service.ICurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider implements ICurrentUserProvider {

  @Override
  public Long getUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
      try {
        return Long.valueOf(jwtAuthentication.getToken().getSubject());
      } catch (NumberFormatException ex) {
        throw new AuthException(ErrorCode.UNAUTHORIZED);
      }
    }
    throw new AuthException(ErrorCode.UNAUTHORIZED);
  }
}
