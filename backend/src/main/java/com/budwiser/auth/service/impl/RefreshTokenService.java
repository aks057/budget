package com.budwiser.auth.service.impl;

import com.budwiser.auth.constant.AuthConstants;
import com.budwiser.auth.entity.RefreshToken;
import com.budwiser.auth.model.IssuedRefreshToken;
import com.budwiser.auth.model.RefreshRotation;
import com.budwiser.auth.repository.IRefreshTokenRepository;
import com.budwiser.auth.service.IRefreshTokenService;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.AuthException;
import com.budwiser.security.properties.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {
  private static final int TOKEN_BYTES = 32;
  private static final String HASH_ALGORITHM = "SHA-256";

  private final IRefreshTokenRepository refreshTokenRepository;
  private final JwtProperties jwtProperties;
  private final Clock clock;
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  @Transactional(rollbackFor = Exception.class)
  public IssuedRefreshToken issue(Long userId, String userAgent) {
    return createToken(userId, UUID.randomUUID(), userAgent);
  }

  /**
   * noRollbackFor: on reuse detection we revoke the family AND throw — the revocation must still commit.
   */
  @Override
  @Transactional(rollbackFor = Exception.class, noRollbackFor = AuthException.class)
  public RefreshRotation rotate(String rawToken, String userAgent) {
    RefreshToken current = refreshTokenRepository.findByTokenHash(hash(rawToken))
      .orElseThrow(() -> new AuthException(ErrorCode.INVALID_REFRESH_TOKEN));
    long now = clock.millis();

    if (current.getRevokedAt() != null) {
      int revoked = refreshTokenRepository.revokeFamily(current.getFamilyId(), now);
      log.warn("[rotate] revoked refresh token reused, family revoked, userId: {}, tokensRevoked: {}",
        current.getUserId(), revoked);
      throw new AuthException(ErrorCode.REFRESH_TOKEN_REUSED);
    }
    if (current.getExpiresAt() <= now) {
      throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    current.setRevokedAt(now);
    IssuedRefreshToken next = createToken(current.getUserId(), current.getFamilyId(), userAgent);
    return new RefreshRotation(current.getUserId(), next);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void revokeFamily(String rawToken) {
    refreshTokenRepository.findByTokenHash(hash(rawToken))
      .ifPresent(token -> {
        refreshTokenRepository.revokeFamily(token.getFamilyId(), clock.millis());
        log.info("[revokeFamily] session revoked, userId: {}", token.getUserId());
      });
  }

  private IssuedRefreshToken createToken(Long userId, UUID familyId, String userAgent) {
    String rawToken = generateRawToken();
    Duration ttl = jwtProperties.refreshTokenTtl();

    RefreshToken token = new RefreshToken();
    token.setUserId(userId);
    token.setTokenHash(hash(rawToken));
    token.setFamilyId(familyId);
    token.setExpiresAt(clock.millis() + ttl.toMillis());
    token.setUserAgent(truncate(userAgent));
    refreshTokenRepository.save(token);

    return new IssuedRefreshToken(rawToken, ttl);
  }

  private String generateRawToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String hash(String rawToken) {
    try {
      byte[] digest = MessageDigest.getInstance(HASH_ALGORITHM).digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 not available", ex);
    }
  }

  private static String truncate(String userAgent) {
    if (userAgent == null || userAgent.length() <= AuthConstants.USER_AGENT_MAX_LENGTH) {
      return userAgent;
    }
    return userAgent.substring(0, AuthConstants.USER_AGENT_MAX_LENGTH);
  }
}
