package com.budwiser.security.config;

import com.budwiser.security.oauth.OAuth2LoginFailureHandler;
import com.budwiser.security.oauth.OAuth2LoginSuccessHandler;
import com.budwiser.security.web.AuthenticatedUserMdcFilter;
import com.budwiser.security.web.RestAccessDeniedHandler;
import com.budwiser.security.web.RestAuthenticationEntryPoint;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless resource server: every API call carries a Bearer RS256 JWT. CSRF is off because auth never rides on a
 * cookie except the refresh cookie, which is SameSite=Strict and scoped to /api/v1/auth.
 * Google login is enabled only when a Google client registration is configured (the "google" profile).
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private static final String[] PUBLIC_PATHS = {
    "/actuator/health",
    "/actuator/health/**",
    "/actuator/info",
    "/api/v1/auth/**",
    "/oauth2/**",
    "/login/oauth2/**",
    "/error"
  };

  private final RestAuthenticationEntryPoint authenticationEntryPoint;
  private final RestAccessDeniedHandler accessDeniedHandler;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
  private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
  private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .httpBasic(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable)
      .logout(AbstractHttpConfigurer::disable)
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        // Re-dispatches of an already-authorized request (SSE completion, error pages) are not re-checked.
        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
        .requestMatchers(PUBLIC_PATHS).permitAll()
        .anyRequest().authenticated())
      .oauth2ResourceServer(resourceServer -> resourceServer
        .jwt(Customizer.withDefaults())
        .authenticationEntryPoint(authenticationEntryPoint)
        .accessDeniedHandler(accessDeniedHandler))
      .exceptionHandling(exceptions -> exceptions
        .authenticationEntryPoint(authenticationEntryPoint)
        .accessDeniedHandler(accessDeniedHandler))
      .addFilterAfter(new AuthenticatedUserMdcFilter(), BearerTokenAuthenticationFilter.class);

    if (clientRegistrationRepository.getIfAvailable() != null) {
      http.oauth2Login(oauth -> oauth
        .successHandler(oAuth2LoginSuccessHandler)
        .failureHandler(oAuth2LoginFailureHandler));
    }
    return http.build();
  }
}
