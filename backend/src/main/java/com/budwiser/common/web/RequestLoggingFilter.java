package com.budwiser.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Outermost filter: assigns a request id (MDC + response header) and logs one access line per request
 * with method, path, status, latency and the authenticated user id.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  public static final String MDC_REQUEST_ID = "requestId";
  public static final String MDC_USER_ID = "userId";
  /** Set by the security layer once the JWT is validated; read here after the chain completes. */
  public static final String USER_ID_ATTRIBUTE = RequestLoggingFilter.class.getName() + ".userId";

  private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");
  private static final String ACTUATOR_PREFIX = "/actuator";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws ServletException, IOException {
    String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
    long startNanos = System.nanoTime();
    MDC.put(MDC_REQUEST_ID, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);
    try {
      chain.doFilter(request, response);
    } finally {
      long latencyMs = (System.nanoTime() - startNanos) / 1_000_000;
      Object userId = request.getAttribute(USER_ID_ATTRIBUTE);
      if (userId != null) {
        MDC.put(MDC_USER_ID, userId.toString());
      }
      log.info("[doFilterInternal] {} {} status: {}, latencyMs: {}",
        request.getMethod(), request.getRequestURI(), response.getStatus(), latencyMs);
      MDC.remove(MDC_USER_ID);
      MDC.remove(MDC_REQUEST_ID);
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return request.getRequestURI().startsWith(ACTUATOR_PREFIX);
  }

  /** Accept a caller-supplied id only if it is short and safe (prevents log injection). */
  private static String resolveRequestId(String headerValue) {
    if (headerValue != null && SAFE_REQUEST_ID.matcher(headerValue).matches()) {
      return headerValue;
    }
    return UUID.randomUUID().toString();
  }
}
