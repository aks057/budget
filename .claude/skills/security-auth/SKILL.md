---
name: security-auth
description: Security conventions for the Bud-Wiser backend — self-issued RS256 JWTs via JwtEncoder, validated by Spring OAuth2 Resource Server, rotating refresh-token cookie, Google OAuth2 login, userId from SecurityContext only, agent tool safety, never log secrets. Apply when adding auth, securing endpoints, reading the current user, or adding agent tools.
---

# Security & Authentication

## Model
- One Spring Boot app. It **issues** short-lived **RS256 access JWTs** (15 min) with `JwtEncoder` (Nimbus) on:
  - `POST /api/v1/auth/register` and `/login`: email + BCrypt password
  - Google OAuth2 login success: `OAuth2LoginSuccessHandler` finds or creates the user and issues tokens
- The same app **validates** them as an OAuth2 **Resource Server**: `.oauth2ResourceServer(o -> o.jwt(...))` with a `JwtDecoder` built from the public key. No jjwt and no hand-written filter.
- **Refresh token:** a random 256-bit opaque value sent in an `httpOnly; Secure; SameSite=Strict; Path=/api/v1/auth` cookie. Only its SHA-256 hash is stored in `refresh_tokens`. It is rotated on every refresh. If a revoked token is reused, the whole family is revoked.
- Sessions are stateless (`SessionCreationPolicy.STATELESS`). There is one exception: the OAuth2 login handshake needs a short-lived authorization-request store, so use a cookie-based `AuthorizationRequestRepository`, not an HTTP session.
- The RSA keypair is loaded from env or a PEM file (`JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY`). A dev keypair is generated at startup only in the `dev` profile. Never commit keys.

## JWT claims
`sub` = user id (string), `email`, `iss` = `budwiser`, `iat`, `exp`, `scope` = `user`. There is no PII beyond the email.

## Security config skeleton
```java
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())            // stateless bearer tokens; refresh cookie is SameSite=Strict
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
        .requestMatchers("/api/v1/auth/**", "/oauth2/**", "/login/oauth2/**").permitAll()
        .requestMatchers("/internal/**").access(internalSecretAuthorizationManager)
        .anyRequest().authenticated())
      .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
      .oauth2Login(o -> o.successHandler(oAuth2LoginSuccessHandler));
    return http.build();
  }
}
```

## Reading the current user
Use `ICurrentUserProvider` in `com.budwiser.security`. It reads `Jwt` from `SecurityContextHolder` and returns `Long userId` from `sub`.
Never trust request parameters, request bodies, or **LLM tool arguments** for caller identity.

## Tenant scoping
Every persistence query filters by `user_id` (see jpa-database). Another user's resource yields 404.

## Agent security (Bud-Wiser specific)
1. The LLM can only call tools in `ToolRegistry`. It has no SQL tool, no HTTP-fetch tool, and no reflection.
2. Tool argument schemas **never include `userId`**. `ToolExecutor` injects the authenticated user.
3. Each argument is deserialized into a DTO and passes Bean Validation and business rules before it executes.
4. **Write tools never execute directly.** They create a `PENDING_CONFIRMATION` row in `agent_actions`. Only `POST /api/v1/agent/actions/{id}/confirm` by the same user executes it. The action must be unexpired and must not already be executed, which makes confirmation idempotent.
5. Treat user-entered text (transaction descriptions, goal names) as untrusted. Wrap it as data in tool results and never concatenate it into the system prompt.
6. Rate-limit agent requests per user (Bucket4j).

## Best practices
1. Get identity from `SecurityContextHolder`, never from request params.
2. Only health/info, auth and OAuth endpoints are public.
3. **Never log** tokens, refresh tokens, passwords, API keys, full LLM prompts or responses, or JWTs.
4. Toggle auth off only through an explicit `no-auth` profile, never silently in code.
5. Keep access-token TTLs short, use BCrypt with the default strength, and use a constant-time path for login failures. The error stays generic, "Invalid email or password".
