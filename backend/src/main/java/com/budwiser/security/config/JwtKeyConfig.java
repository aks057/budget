package com.budwiser.security.config;

import com.budwiser.security.properties.JwtProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StringUtils;

/**
 * RS256 key material. The private key signs access tokens; the public key validates them in the resource server.
 * Production keys come from env (PEM); dev/test may generate an ephemeral pair.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtKeyConfig {
  private static final String KEY_ALGORITHM = "RSA";
  private static final int DEV_KEY_SIZE = 2048;
  private static final String KEY_ID = "budwiser-access-v1";

  private final JwtProperties jwtProperties;

  @Bean
  public RSAKey jwtSigningKey() {
    if (StringUtils.hasText(jwtProperties.privateKey()) && StringUtils.hasText(jwtProperties.publicKey())) {
      return new RSAKey.Builder(parsePublicKey(jwtProperties.publicKey()))
        .privateKey(parsePrivateKey(jwtProperties.privateKey()))
        .keyID(KEY_ID)
        .build();
    }
    if (jwtProperties.generateDevKeys()) {
      log.warn("[jwtSigningKey] no JWT keys configured, generating an ephemeral pair; tokens will not survive restarts");
      return generateKey();
    }
    throw new IllegalStateException("JWT keys missing: set JWT_PRIVATE_KEY and JWT_PUBLIC_KEY");
  }

  @Bean
  public JwtEncoder jwtEncoder(RSAKey jwtSigningKey) {
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwtSigningKey)));
  }

  @Bean
  public JwtDecoder jwtDecoder(RSAKey jwtSigningKey) throws JOSEException {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(jwtSigningKey.toRSAPublicKey()).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));
    return decoder;
  }

  private static RSAKey generateKey() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
      generator.initialize(DEV_KEY_SIZE);
      KeyPair keyPair = generator.generateKeyPair();
      return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
        .privateKey((RSAPrivateKey) keyPair.getPrivate())
        .keyID(KEY_ID)
        .build();
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Unable to generate RSA key pair", ex);
    }
  }

  private static RSAPublicKey parsePublicKey(String pem) {
    try {
      return (RSAPublicKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(decodePem(pem)));
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Invalid JWT_PUBLIC_KEY (expected X.509 'BEGIN PUBLIC KEY' PEM)", ex);
    }
  }

  private static RSAPrivateKey parsePrivateKey(String pem) {
    try {
      return (RSAPrivateKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decodePem(pem)));
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Invalid JWT_PRIVATE_KEY (expected PKCS#8 'BEGIN PRIVATE KEY' PEM)", ex);
    }
  }

  /** Accepts real newlines or literal "\n" (common when PEMs are passed through env vars). */
  private static byte[] decodePem(String pem) {
    String base64 = pem
      .replace("\\n", "")
      .replaceAll("-----(BEGIN|END) [A-Z ]+-----", "")
      .replaceAll("\\s", "");
    return Base64.getDecoder().decode(base64);
  }
}
