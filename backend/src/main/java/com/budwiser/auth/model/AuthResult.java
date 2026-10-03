package com.budwiser.auth.model;

import com.budwiser.auth.dto.AuthResponse;

/**
 * Service-layer result: the JSON body plus the refresh token the web layer writes into a cookie.
 */
public record AuthResult(AuthResponse response, IssuedRefreshToken refreshToken) {}
