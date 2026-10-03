package com.budwiser.auth.model;

public record RefreshRotation(Long userId, IssuedRefreshToken nextToken) {}
