package com.budwiser.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budwiser.auth.dto.LoginRequest;
import com.budwiser.auth.entity.User;
import com.budwiser.auth.mapper.IUserMapper;
import com.budwiser.auth.model.GoogleProfile;
import com.budwiser.auth.model.IssuedAccessToken;
import com.budwiser.auth.repository.IUserRepository;
import com.budwiser.auth.service.IRefreshTokenService;
import com.budwiser.auth.service.ITokenService;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.AuthException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
  private static final String DUMMY_HASH = "$2a$10$dummyhashdummyhashdummyhashdummyhashdummyhashdummyha";

  private IUserRepository mockUserRepository;
  private IRefreshTokenService mockRefreshTokenService;
  private PasswordEncoder mockPasswordEncoder;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    mockUserRepository = mock(IUserRepository.class);
    mockRefreshTokenService = mock(IRefreshTokenService.class);
    mockPasswordEncoder = mock(PasswordEncoder.class);
    when(mockPasswordEncoder.encode(anyString())).thenReturn(DUMMY_HASH);
    ITokenService mockTokenService = mock(ITokenService.class);
    when(mockTokenService.issueAccessToken(any())).thenReturn(new IssuedAccessToken("access-token", 900));
    authService = new AuthService(mockUserRepository, mockRefreshTokenService, mockTokenService,
      mock(IUserMapper.class), mockPasswordEncoder, mock(ApplicationEventPublisher.class));
  }

  @Test
  @DisplayName("login with an unknown email still runs a BCrypt comparison (no timing oracle)")
  void loginUnknownEmailStillHashes() {
    when(mockUserRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest("Ghost@Example.com ", "whatever-123"), "JUnit"))
      .isInstanceOf(AuthException.class)
      .hasMessage(ErrorCode.INVALID_CREDENTIALS.getDescription());
    verify(mockPasswordEncoder).matches("whatever-123", DUMMY_HASH);
    verify(mockRefreshTokenService, never()).issue(any(), any());
  }

  @Test
  @DisplayName("a Google-only account (no password) cannot log in with a password")
  void loginGoogleOnlyAccount() {
    User googleUser = new User();
    googleUser.setEmail("g@example.com");
    googleUser.setGoogleSubject("google-sub");
    when(mockUserRepository.findByEmail("g@example.com")).thenReturn(Optional.of(googleUser));

    assertThatThrownBy(() -> authService.login(new LoginRequest("g@example.com", "anything-123"), "JUnit"))
      .isInstanceOf(AuthException.class)
      .hasMessage(ErrorCode.INVALID_CREDENTIALS.getDescription());
  }

  @Test
  @DisplayName("Google login is refused when Google reports the email as unverified")
  void googleUnverifiedEmail() {
    GoogleProfile profile = new GoogleProfile("sub-1", "a@example.com", false, "A", null);

    assertThatThrownBy(() -> authService.loginWithGoogle(profile, "JUnit"))
      .isInstanceOf(AuthException.class)
      .hasMessage(ErrorCode.OAUTH_EMAIL_NOT_VERIFIED.getDescription());
    verify(mockUserRepository, never()).save(any());
  }

  @Test
  @DisplayName("Google login links to an existing password account with the same verified email")
  void googleLinksExistingAccount() {
    User existing = new User();
    existing.setId(7L);
    existing.setEmail("a@example.com");
    existing.setPasswordHash(DUMMY_HASH);
    when(mockUserRepository.findByGoogleSubject("sub-1")).thenReturn(Optional.empty());
    when(mockUserRepository.findByEmail("a@example.com")).thenReturn(Optional.of(existing));

    authService.loginWithGoogle(new GoogleProfile("sub-1", "A@example.com", true, "A", "https://pic"), "JUnit");

    assertThat(existing.getGoogleSubject()).isEqualTo("sub-1");
    assertThat(existing.getAvatarUrl()).isEqualTo("https://pic");
    verify(mockUserRepository, never()).save(any());
    verify(mockRefreshTokenService).issue(eq(7L), eq("JUnit"));
  }
}
