package com.budwiser.common.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * In-memory token buckets (Bucket4j), one per policy+key, held in a bounded, expiring Caffeine cache so idle keys
 * don't leak memory. Correct for a single instance; with several instances the buckets would move to Redis
 * (Bucket4j has a Redis backend) — the interface stays the same.
 */
@Component
public class RateLimiter {
  private static final long MAX_TRACKED_KEYS = 100_000;
  private static final Duration IDLE_EXPIRY = Duration.ofMinutes(30);

  private final RateLimitProperties properties;
  private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
    .maximumSize(MAX_TRACKED_KEYS)
    .expireAfterAccess(IDLE_EXPIRY)
    .build();

  public RateLimiter(RateLimitProperties properties) {
    this.properties = properties;
  }

  public record Decision(boolean allowed, long remaining, long retryAfterSeconds) {}

  public Decision tryConsume(RateLimitPolicy policy, String key) {
    Bucket bucket = buckets.get(policy.name() + ":" + key, ignored -> newBucket(properties.forPolicy(policy)));
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    long retryAfter = probe.isConsumed() ? 0 : Math.max(1, Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds());
    return new Decision(probe.isConsumed(), probe.getRemainingTokens(), retryAfter);
  }

  private static Bucket newBucket(RateLimitProperties.Limit limit) {
    return Bucket.builder()
      .addLimit(bandwidth -> bandwidth.capacity(limit.capacity()).refillGreedy(limit.capacity(), limit.period()))
      .build();
  }
}
