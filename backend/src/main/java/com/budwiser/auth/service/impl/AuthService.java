package com.budwiser.auth.service.impl;

import com.budwiser.auth.constant.AuthConstants;
import com.budwiser.auth.dto.AuthResponse;
import com.budwiser.auth.dto.LoginRequest;
import com.budwiser.auth.dto.RegisterRequest;
import com.budwiser.auth.entity.User;
import com.budwiser.auth.event.UserRegisteredEvent;
import com.budwiser.auth.mapper.IUserMapper;
import com.budwiser.auth.model.AuthResult;
import com.budwiser.auth.model.GoogleProfile;
import com.budwiser.auth.model.IssuedAccessToken;
import com.budwiser.auth.model.IssuedRefreshToken;
import com.budwiser.auth.model.RefreshRotation;
import com.budwiser.auth.repository.IUserRepository;
import com.budwiser.auth.service.IAuthService;
import com.budwiser.auth.service.IRefreshTokenService;
import com.budwiser.auth.service.ITokenService;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.AuthException;
import com.budwiser.common.exception.ConflictException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class AuthService implements IAuthService {
  private final IUserRepository userRepository;
  private final IRefreshTokenService refreshTokenService;
  private final ITokenService tokenService;
  private final IUserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;
  /** Compared against when the email is unknown so login timing doesn't reveal which emails are registered. */
  private final String dummyPasswordHash;

  public AuthService(IUserRepository userRepository, IRefreshTokenService refreshTokenService,
                     ITokenService tokenService, IUserMapper userMapper, PasswordEncoder passwordEncoder,
                     ApplicationEventPublisher eventPublisher) {
    this.userRepository = userRepository;
    this.refreshTokenService = refreshTokenService;
    this.tokenService = tokenService;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.eventPublisher = eventPublisher;
    this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AuthResult register(RegisterRequest request, String userAgent) {
    String email = normalizeEmail(request.getEmail());
    if (userRepository.existsByEmail(email)) {
      throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
    }
    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setFullName(request.getFullName().trim());
    user.setCurrency(AuthConstants.DEFAULT_CURRENCY);
    try {
      userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException ex) {
      // Two concurrent registrations both passed existsByEmail; the unique index decides — translate, don't swallow.
      throw new ConflictException(ErrorCode.EMAIL_ALREADY_REGISTERED);
    }
    eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));
    log.info("[register] user registered, userId: {}", user.getId());
    return issueSession(user, userAgent);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AuthResult login(LoginRequest request, String userAgent) {
    Optional<User> user = userRepository.findByEmail(normalizeEmail(request.getEmail()));
    String storedHash = user.map(User::getPasswordHash).orElse(null);
    boolean passwordMatches = passwordEncoder.matches(request.getPassword(),
      storedHash != null ? storedHash : dummyPasswordHash);
    if (storedHash == null || !passwordMatches) {
      throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
    }
    log.info("[login] password login, userId: {}", user.get().getId());
    return issueSession(user.get(), userAgent);
  }

  /**
   * Links by Google subject first, then by verified email (so a password user can later "Continue with Google").
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public AuthResult loginWithGoogle(GoogleProfile profile, String userAgent) {
    if (!profile.emailVerified()) {
      throw new AuthException(ErrorCode.OAUTH_EMAIL_NOT_VERIFIED);
    }
    String email = normalizeEmail(profile.email());
    User user = userRepository.findByGoogleSubject(profile.subject())
      .orElseGet(() -> userRepository.findByEmail(email)
        .map(existing -> linkGoogle(existing, profile))
        .orElseGet(() -> createGoogleUser(email, profile)));
    log.info("[loginWithGoogle] google login, userId: {}", user.getId());
    return issueSession(user, userAgent);
  }

  /**
   * Deliberately NOT @Transactional: rotation commits on its own (including a family revocation on reuse),
   * so an outer rollback can never undo the revocation.
   */
  @Override
  public AuthResult refresh(String rawRefreshToken, String userAgent) {
    if (!StringUtils.hasText(rawRefreshToken)) {
      throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
    RefreshRotation rotation = refreshTokenService.rotate(rawRefreshToken, userAgent);
    User user = userRepository.findById(rotation.userId())
      .orElseThrow(() -> new AuthException(ErrorCode.INVALID_REFRESH_TOKEN));
    return new AuthResult(buildResponse(user), rotation.nextToken());
  }

  @Override
  public void logout(String rawRefreshToken) {
    if (StringUtils.hasText(rawRefreshToken)) {
      refreshTokenService.revokeFamily(rawRefreshToken);
    }
  }

  private User linkGoogle(User existing, GoogleProfile profile) {
    existing.setGoogleSubject(profile.subject());
    if (existing.getAvatarUrl() == null) {
      existing.setAvatarUrl(profile.pictureUrl());
    }
    log.info("[linkGoogle] google account linked, userId: {}", existing.getId());
    return existing;
  }

  private User createGoogleUser(String email, GoogleProfile profile) {
    User user = new User();
    user.setEmail(email);
    user.setGoogleSubject(profile.subject());
    user.setFullName(StringUtils.hasText(profile.fullName()) ? profile.fullName().trim() : email);
    user.setAvatarUrl(profile.pictureUrl());
    user.setCurrency(AuthConstants.DEFAULT_CURRENCY);
    userRepository.save(user);
    eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));
    return user;
  }

  private AuthResult issueSession(User user, String userAgent) {
    IssuedRefreshToken refreshToken = refreshTokenService.issue(user.getId(), userAgent);
    return new AuthResult(buildResponse(user), refreshToken);
  }

  private AuthResponse buildResponse(User user) {
    IssuedAccessToken accessToken = tokenService.issueAccessToken(user);
    return AuthResponse.builder()
      .accessToken(accessToken.token())
      .tokenType(AuthConstants.TOKEN_TYPE_BEARER)
      .expiresIn(accessToken.expiresInSeconds())
      .user(userMapper.toDto(user))
      .build();
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
