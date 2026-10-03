package com.budwiser.security.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Hands filter-level 401s to the MVC exception resolver so they get the same {@code Response} body as controller errors.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final HandlerExceptionResolver handlerExceptionResolver;

  public RestAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
    handlerExceptionResolver.resolveException(request, response, null, authException);
  }
}
