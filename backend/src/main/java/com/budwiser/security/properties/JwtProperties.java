package com.budwiser.security.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param privateKey      PKCS#8 PEM (BEGIN PRIVATE KEY), from env JWT_PRIVATE_KEY
 * @param publicKey       X.509 PEM (BEGIN PUBLIC KEY), from env JWT_PUBLIC_KEY
 * @param generateDevKeys generate an ephemeral key pair when keys are absent — dev/test profiles only
 */
@Validated
@ConfigurationProperties(prefix = "budwiser.security.jwt")
public record JwtProperties(
  @NotBlank String issuer,
  @NotNull Duration accessTokenTtl,
  @NotNull Duration refreshTokenTtl,
  String privateKey,
  String publicKey,
  boolean generateDevKeys
) {}
