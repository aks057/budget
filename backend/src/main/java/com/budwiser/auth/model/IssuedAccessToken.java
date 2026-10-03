package com.budwiser.auth.model;

public record IssuedAccessToken(String token, long expiresInSeconds) {}
