package com.budwiser.common.ratelimit;

public enum RateLimitPolicy {
  /** Per authenticated user: protects LLM cost (PRD §23). */
  AGENT,
  /** Per client IP: slows credential stuffing / brute force on login, register and refresh. */
  AUTH
}
