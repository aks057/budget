package com.budwiser.auth.model;

public record GoogleProfile(String subject, String email, boolean emailVerified, String fullName, String pictureUrl) {}
