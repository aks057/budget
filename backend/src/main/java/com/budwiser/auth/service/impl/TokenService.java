package com.budwiser.auth.service.impl;

import com.budwiser.auth.constant.AuthConstants;
import com.budwiser.auth.entity.User;
import com.budwiser.auth.model.IssuedAccessToken;
import com.budwiser.auth.service.ITokenService;
import com.budwiser.security.properties.JwtProperties;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Issues short-lived RS256 access tokens. Validation is done by Spring's OAuth2 Resource Server with the public key.
 */
@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {
  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;
  private final Clock clock;

  @Override
  public IssuedAccessToken issueAccessToken(User user) {
    Instant now = clock.instant();
    JwtClaimsSet claims = JwtClaimsSet.builder()
      .issuer(jwtProperties.issuer())
      .subject(String.valueOf(user.getId()))
      .issuedAt(now)
      .expiresAt(now.plus(jwtProperties.accessTokenTtl()))
      .claim(AuthConstants.CLAIM_EMAIL, user.getEmail())
      .claim(AuthConstants.CLAIM_SCOPE, AuthConstants.SCOPE_USER)
      .build();
    JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
    String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new IssuedAccessToken(token, jwtProperties.accessTokenTtl().toSeconds());
  }
}
