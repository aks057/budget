package com.budwiser.auth.event;

/**
 * Published inside the registration transaction; listeners run synchronously, so if one fails the signup rolls back.
 */
public record UserRegisteredEvent(Long userId) {}
