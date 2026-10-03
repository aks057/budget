package com.budwiser.common.ratelimit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "budwiser.rate-limit")
public record RateLimitProperties(@Valid @NotNull Limit agent, @Valid @NotNull Limit auth) {

  /** {@code capacity} requests per {@code period}, refilled gradually (token bucket). */
  public record Limit(@Min(1) int capacity, @NotNull Duration period) {}

  public Limit forPolicy(RateLimitPolicy policy) {
    return switch (policy) {
      case AGENT -> agent;
      case AUTH -> auth;
    };
  }
}
