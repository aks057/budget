package com.budwiser.security.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Filter-level 403s, rendered by the global exception handler.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
  private final HandlerExceptionResolver handlerExceptionResolver;

  public RestAccessDeniedHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
    handlerExceptionResolver.resolveException(request, response, null, accessDeniedException);
  }
}
