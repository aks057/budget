package com.budwiser.security.web;

import com.budwiser.common.web.RequestLoggingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Runs inside the security chain right after bearer-token authentication: puts the user id into the MDC
 * (so every log line of the request carries it) and onto the request for the outer access-log filter.
 * Not a @Component — it is registered only inside the security filter chain.
 */
public class AuthenticatedUserMdcFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws ServletException, IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
      chain.doFilter(request, response);
      return;
    }
    String userId = jwtAuthentication.getName();
    MDC.put(RequestLoggingFilter.MDC_USER_ID, userId);
    request.setAttribute(RequestLoggingFilter.USER_ID_ATTRIBUTE, userId);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(RequestLoggingFilter.MDC_USER_ID);
    }
  }
}
