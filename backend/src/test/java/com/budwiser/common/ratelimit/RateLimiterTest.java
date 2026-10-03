package com.budwiser.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimiterTest {
  private final RateLimiter rateLimiter = new RateLimiter(new RateLimitProperties(
    new RateLimitProperties.Limit(3, Duration.ofMinutes(1)),
    new RateLimitProperties.Limit(2, Duration.ofMinutes(1))));

  @Test
  @DisplayName("allows up to capacity, then rejects with a retry-after hint")
  void capacity() {
    for (int i = 0; i < 3; i++) {
      assertThat(rateLimiter.tryConsume(RateLimitPolicy.AGENT, "user:1").allowed()).isTrue();
    }
    RateLimiter.Decision rejected = rateLimiter.tryConsume(RateLimitPolicy.AGENT, "user:1");

    assertThat(rejected.allowed()).isFalse();
    assertThat(rejected.retryAfterSeconds()).isBetween(1L, 60L);
  }

  @Test
  @DisplayName("buckets are independent per key and per policy")
  void independentBuckets() {
    rateLimiter.tryConsume(RateLimitPolicy.AUTH, "ip:10.0.0.1");
    rateLimiter.tryConsume(RateLimitPolicy.AUTH, "ip:10.0.0.1");

    assertThat(rateLimiter.tryConsume(RateLimitPolicy.AUTH, "ip:10.0.0.1").allowed()).isFalse();
    assertThat(rateLimiter.tryConsume(RateLimitPolicy.AUTH, "ip:10.0.0.2").allowed()).isTrue();
    assertThat(rateLimiter.tryConsume(RateLimitPolicy.AGENT, "ip:10.0.0.1").allowed()).isTrue();
  }
}
