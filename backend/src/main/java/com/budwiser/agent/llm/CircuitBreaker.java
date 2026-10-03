package com.budwiser.agent.llm;

import java.time.Clock;
import java.time.Duration;

/**
 * Per-provider circuit breaker. After {@code failureThreshold} consecutive failures the provider is skipped
 * (OPEN) for {@code openDuration} — no 30s timeout paid on every request. Then one trial request is let through
 * (HALF_OPEN): success closes the circuit, failure re-opens it.
 */
public class CircuitBreaker {

  public enum State {
    CLOSED,
    OPEN,
    HALF_OPEN
  }

  private final int failureThreshold;
  private final Duration openDuration;
  private final Clock clock;

  private State state = State.CLOSED;
  private int consecutiveFailures;
  private long openedAtMillis;
  private boolean trialInFlight;

  public CircuitBreaker(int failureThreshold, Duration openDuration, Clock clock) {
    if (failureThreshold < 1) {
      throw new IllegalArgumentException("failureThreshold must be >= 1");
    }
    this.failureThreshold = failureThreshold;
    this.openDuration = openDuration;
    this.clock = clock;
  }

  /** Whether a request may be sent now. In HALF_OPEN only a single trial request is admitted. */
  public synchronized boolean tryAcquire() {
    switch (state) {
      case CLOSED:
        return true;
      case OPEN:
        if (clock.millis() - openedAtMillis < openDuration.toMillis()) {
          return false;
        }
        state = State.HALF_OPEN;
        trialInFlight = true;
        return true;
      case HALF_OPEN:
      default:
        if (trialInFlight) {
          return false;
        }
        trialInFlight = true;
        return true;
    }
  }

  public synchronized void recordSuccess() {
    state = State.CLOSED;
    consecutiveFailures = 0;
    trialInFlight = false;
  }

  public synchronized void recordFailure() {
    trialInFlight = false;
    if (state == State.HALF_OPEN) {
      open();
      return;
    }
    consecutiveFailures++;
    if (consecutiveFailures >= failureThreshold) {
      open();
    }
  }

  public synchronized State state() {
    return state;
  }

  private void open() {
    state = State.OPEN;
    openedAtMillis = clock.millis();
    consecutiveFailures = 0;
  }
}
