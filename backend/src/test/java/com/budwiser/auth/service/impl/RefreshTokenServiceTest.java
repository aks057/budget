package com.budwiser.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budwiser.auth.entity.RefreshToken;
import com.budwiser.auth.model.RefreshRotation;
import com.budwiser.auth.repository.IRefreshTokenRepository;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.AuthException;
import com.budwiser.security.properties.JwtProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RefreshTokenServiceTest {
  private static final Instant NOW = Instant.parse("2026-10-02T10:00:00Z");
  private static final Duration REFRESH_TTL = Duration.ofDays(30);
  private static final Long USER_ID = 42L;

  private IRefreshTokenRepository mockRepository;
  private RefreshTokenService refreshTokenService;

  @BeforeEach
  void setUp() {
    mockRepository = mock(IRefreshTokenRepository.class);
    JwtProperties jwtProperties = new JwtProperties("budwiser", Duration.ofMinutes(15), REFRESH_TTL, null, null, true);
    refreshTokenService = new RefreshTokenService(mockRepository, jwtProperties, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  @DisplayName("issue stores only a SHA-256 hash, never the raw token, and starts a new family")
  void issueStoresHashOnly() {
    var actual = refreshTokenService.issue(USER_ID, "JUnit");

    ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
    verify(mockRepository).save(saved.capture());
    assertThat(saved.getValue().getTokenHash()).hasSize(64).isNotEqualTo(actual.rawToken());
    assertThat(saved.getValue().getFamilyId()).isNotNull();
    assertThat(saved.getValue().getExpiresAt()).isEqualTo(NOW.plus(REFRESH_TTL).toEpochMilli());
    assertThat(actual.timeToLive()).isEqualTo(REFRESH_TTL);
  }

  @Test
  @DisplayName("rotate revokes the presented token and issues a successor in the same family")
  void rotateHappyPath() {
    RefreshToken current = activeToken();
    when(mockRepository.findByTokenHash(anyString())).thenReturn(Optional.of(current));

    RefreshRotation actual = refreshTokenService.rotate("raw-token", "JUnit");

    assertThat(actual.userId()).isEqualTo(USER_ID);
    assertThat(current.getRevokedAt()).isEqualTo(NOW.toEpochMilli());
    ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
    verify(mockRepository).save(saved.capture());
    assertThat(saved.getValue().getFamilyId()).isEqualTo(current.getFamilyId());
  }

  @Test
  @DisplayName("rotating an already-revoked token revokes the family and throws REFRESH_TOKEN_REUSED")
  void rotateReuseDetected() {
    RefreshToken revoked = activeToken();
    revoked.setRevokedAt(NOW.minusSeconds(60).toEpochMilli());
    when(mockRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revoked));

    assertThatThrownBy(() -> refreshTokenService.rotate("stolen-token", "JUnit"))
      .isInstanceOf(AuthException.class)
      .hasMessage(ErrorCode.REFRESH_TOKEN_REUSED.getDescription());
    verify(mockRepository).revokeFamily(revoked.getFamilyId(), NOW.toEpochMilli());
    verify(mockRepository, never()).save(any());
  }

  @Test
  @DisplayName("an expired token is rejected without issuing a successor")
  void rotateExpired() {
    RefreshToken expired = activeToken();
    expired.setExpiresAt(NOW.toEpochMilli());
    when(mockRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> refreshTokenService.rotate("old-token", "JUnit"))
      .isInstanceOf(AuthException.class)
      .hasMessage(ErrorCode.INVALID_REFRESH_TOKEN.getDescription());
    verify(mockRepository, never()).save(any());
  }

  @Test
  @DisplayName("an unknown token is rejected")
  void rotateUnknown() {
    when(mockRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> refreshTokenService.rotate("unknown", "JUnit"))
      .isInstanceOf(AuthException.class);
  }

  @Test
  @DisplayName("revokeFamily with an unknown token is a no-op (idempotent logout)")
  void revokeFamilyUnknown() {
    when(mockRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    refreshTokenService.revokeFamily("unknown");

    verify(mockRepository, never()).revokeFamily(any(), anyLong());
  }

  private static RefreshToken activeToken() {
    RefreshToken token = new RefreshToken();
    token.setUserId(USER_ID);
    token.setFamilyId(UUID.randomUUID());
    token.setTokenHash("a".repeat(64));
    token.setExpiresAt(NOW.plus(Duration.ofDays(1)).toEpochMilli());
    return token;
  }
}
