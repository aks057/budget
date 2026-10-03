package com.budwiser.common.ratelimit;

import com.budwiser.common.exception.RateLimitExceededException;
import com.budwiser.security.service.ICurrentUserProvider;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enforces {@link RateLimit}. Runs after Spring Security, so AGENT limits key on the authenticated user id.
 * AUTH limits key on the client address (behind Caddy, forward-headers handling makes this the real client IP).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
  public static final String REMAINING_HEADER = "X-RateLimit-Remaining";

  private final RateLimiter rateLimiter;
  private final ICurrentUserProvider currentUserProvider;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // An SSE/async request is dispatched a second time when it completes — count it only once.
    if (request.getDispatcherType() == DispatcherType.ASYNC || !(handler instanceof HandlerMethod method)) {
      return true;
    }
    RateLimit rateLimit = method.getMethodAnnotation(RateLimit.class);
    if (rateLimit == null) {
      return true;
    }
    String key = switch (rateLimit.value()) {
      case AGENT -> "user:" + currentUserProvider.getUserId();
      case AUTH -> "ip:" + request.getRemoteAddr();
    };
    RateLimiter.Decision decision = rateLimiter.tryConsume(rateLimit.value(), key);
    if (!decision.allowed()) {
      log.warn("[preHandle] rate limit exceeded, policy: {}, path: {}", rateLimit.value(), request.getRequestURI());
      throw new RateLimitExceededException(decision.retryAfterSeconds());
    }
    response.setHeader(REMAINING_HEADER, String.valueOf(decision.remaining()));
    return true;
  }
}
