package com.budwiser.auth.model;

import java.time.Duration;

/**
 * Raw refresh token to put in the cookie (never persisted) and its remaining lifetime.
 */
public record IssuedRefreshToken(String rawToken, Duration timeToLive) {}
