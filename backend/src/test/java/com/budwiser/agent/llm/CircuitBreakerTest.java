package com.budwiser.agent.llm;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest {
  private static final Duration OPEN_DURATION = Duration.ofSeconds(60);

  private MutableClock clock;
  private CircuitBreaker breaker;

  @BeforeEach
  void setUp() {
    clock = new MutableClock(Instant.parse("2026-10-02T10:00:00Z"));
    breaker = new CircuitBreaker(3, OPEN_DURATION, clock);
  }

  @Test
  @DisplayName("stays closed below the threshold; a success resets the failure count")
  void closedBelowThreshold() {
    breaker.recordFailure();
    breaker.recordFailure();
    breaker.recordSuccess();
    breaker.recordFailure();
    breaker.recordFailure();

    assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.CLOSED);
    assertThat(breaker.tryAcquire()).isTrue();
  }

  @Test
  @DisplayName("opens after N consecutive failures and rejects until the open period elapses")
  void opensAndWaits() {
    failTimes(3);

    assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.OPEN);
    assertThat(breaker.tryAcquire()).isFalse();
    clock.advance(OPEN_DURATION.minusSeconds(1));
    assertThat(breaker.tryAcquire()).isFalse();
  }

  @Test
  @DisplayName("after the open period exactly one trial is admitted; its success closes the circuit")
  void halfOpenSuccess() {
    failTimes(3);
    clock.advance(OPEN_DURATION);

    assertThat(breaker.tryAcquire()).isTrue();
    assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
    assertThat(breaker.tryAcquire()).as("second concurrent trial").isFalse();
    breaker.recordSuccess();
    assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.CLOSED);
  }

  @Test
  @DisplayName("a failed trial re-opens the circuit for another full period")
  void halfOpenFailure() {
    failTimes(3);
    clock.advance(OPEN_DURATION);
    breaker.tryAcquire();

    breaker.recordFailure();

    assertThat(breaker.state()).isEqualTo(CircuitBreaker.State.OPEN);
    assertThat(breaker.tryAcquire()).isFalse();
  }

  private void failTimes(int times) {
    for (int i = 0; i < times; i++) {
      breaker.recordFailure();
    }
  }

  private static final class MutableClock extends Clock {
    private Instant now;

    private MutableClock(Instant start) {
      this.now = start;
    }

    void advance(Duration duration) {
      now = now.plus(duration);
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }
}
