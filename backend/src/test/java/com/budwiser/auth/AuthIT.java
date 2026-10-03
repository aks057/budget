package com.budwiser.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budwiser.AbstractIntegrationTest;
import com.budwiser.auth.constant.AuthConstants;
import com.budwiser.common.web.RequestLoggingFilter;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class AuthIT extends AbstractIntegrationTest {
  private static final String PASSWORD = "correct-horse-42";
  private static final String REGISTER_URL = "/api/v1/auth/register";
  private static final String LOGIN_URL = "/api/v1/auth/login";
  private static final String REFRESH_URL = "/api/v1/auth/refresh";
  private static final String LOGOUT_URL = "/api/v1/auth/logout";
  private static final String ME_URL = "/api/v1/users/me";

  @Autowired
  private MockMvc mockMvc;

  @Nested
  @DisplayName("register")
  class Register {

    @Test
    @DisplayName("creates the user, returns an access token and sets a hardened refresh cookie")
    void registerSuccess() throws Exception {
      String email = uniqueEmail();

      register(email, PASSWORD)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.data.expiresIn").value(900))
        .andExpect(jsonPath("$.data.user.email").value(email))
        .andExpect(jsonPath("$.data.user.id").isString())
        .andExpect(jsonPath("$.data.user.currency").value("INR"))
        .andExpect(cookie().exists(AuthConstants.REFRESH_COOKIE_NAME))
        .andExpect(cookie().httpOnly(AuthConstants.REFRESH_COOKIE_NAME, true))
        .andExpect(cookie().secure(AuthConstants.REFRESH_COOKIE_NAME, true))
        .andExpect(cookie().sameSite(AuthConstants.REFRESH_COOKIE_NAME, "Strict"))
        .andExpect(cookie().path(AuthConstants.REFRESH_COOKIE_NAME, AuthConstants.REFRESH_COOKIE_PATH));
    }

    @Test
    @DisplayName("rejects a duplicate email case-insensitively with 409")
    void registerDuplicateEmail() throws Exception {
      String email = uniqueEmail();
      register(email, PASSWORD).andExpect(status().isCreated());

      register(email.toUpperCase(), PASSWORD)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.errors[0].code").value("BW-1002"));
    }

    @Test
    @DisplayName("returns 400 with one error per invalid field")
    void registerValidation() throws Exception {
      mockMvc.perform(post(REGISTER_URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
            {"email": "not-an-email", "password": "short", "fullName": ""}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.length()").value(3))
        .andExpect(jsonPath("$.errors[*].errorInfo").value(containsInAnyOrder("email", "password", "fullName")));
    }

    @Test
    @DisplayName("returns 400 for a malformed JSON body")
    void registerMalformedBody() throws Exception {
      mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content("{not json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].code").value("BW-9002"));
    }
  }

  @Nested
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName("succeeds with correct credentials, email matched case-insensitively")
    void loginSuccess() throws Exception {
      String email = uniqueEmail();
      register(email, PASSWORD);

      login(email.toUpperCase(), PASSWORD)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(cookie().exists(AuthConstants.REFRESH_COOKIE_NAME));
    }

    @Test
    @DisplayName("wrong password and unknown email produce the identical generic 401")
    void loginFailuresAreIndistinguishable() throws Exception {
      String email = uniqueEmail();
      register(email, PASSWORD);

      login(email, "wrong-password-1")
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("BW-1001"))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
      login(uniqueEmail(), PASSWORD)
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("BW-1001"))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
  }

  @Nested
  @DisplayName("protected endpoints")
  class ProtectedEndpoints {

    @Test
    @DisplayName("GET /users/me returns the caller identified by the JWT")
    void meWithToken() throws Exception {
      String email = uniqueEmail();
      String accessToken = accessToken(register(email, PASSWORD).andReturn().getResponse());

      mockMvc.perform(get(ME_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").value(email));
    }

    @Test
    @DisplayName("missing token yields a 401 in the standard Response envelope")
    void meWithoutToken() throws Exception {
      mockMvc.perform(get(ME_URL))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.errors[0].code").value("BW-1005"));
    }

    @Test
    @DisplayName("forged or garbage token yields 401")
    void meWithInvalidToken() throws Exception {
      mockMvc.perform(get(ME_URL).header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("BW-1005"));
    }

    @Test
    @DisplayName("every response carries a request id; a safe caller-supplied id is echoed back")
    void requestIdHeader() throws Exception {
      mockMvc.perform(get(ME_URL).header(RequestLoggingFilter.REQUEST_ID_HEADER, "trace-abc-123"))
        .andExpect(header().string(RequestLoggingFilter.REQUEST_ID_HEADER, "trace-abc-123"));
      mockMvc.perform(get(ME_URL).header(RequestLoggingFilter.REQUEST_ID_HEADER, "bad\nid"))
        .andExpect(header().string(RequestLoggingFilter.REQUEST_ID_HEADER, matchesPattern("[0-9a-f-]{36}")));
    }
  }

  @Nested
  @DisplayName("refresh token rotation")
  class Refresh {

    @Test
    @DisplayName("refresh rotates the cookie and returns a fresh access token")
    void refreshRotates() throws Exception {
      String originalRefresh = refreshCookie(register(uniqueEmail(), PASSWORD).andReturn().getResponse());

      MockHttpServletResponse refreshed = refresh(originalRefresh)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andReturn().getResponse();

      assertThat(refreshCookie(refreshed)).isNotBlank().isNotEqualTo(originalRefresh);
    }

    @Test
    @DisplayName("reusing a rotated token is detected and revokes the whole family")
    void reuseRevokesFamily() throws Exception {
      String firstToken = refreshCookie(register(uniqueEmail(), PASSWORD).andReturn().getResponse());
      String secondToken = refreshCookie(refresh(firstToken).andExpect(status().isOk()).andReturn().getResponse());

      // Attacker replays the stolen, already-rotated first token.
      refresh(firstToken)
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("BW-1004"));

      // The legitimate holder's newer token is now dead too.
      refresh(secondToken).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("refresh without a cookie yields 401")
    void refreshWithoutCookie() throws Exception {
      mockMvc.perform(post(REFRESH_URL))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errors[0].code").value("BW-1003"));
    }

    @Test
    @DisplayName("an unknown refresh token yields 401")
    void refreshUnknownToken() throws Exception {
      refresh("this-token-was-never-issued").andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("logout")
  class Logout {

    @Test
    @DisplayName("revokes the session server-side and clears the cookie")
    void logoutRevokes() throws Exception {
      String refreshToken = refreshCookie(register(uniqueEmail(), PASSWORD).andReturn().getResponse());

      mockMvc.perform(post(LOGOUT_URL).cookie(new Cookie(AuthConstants.REFRESH_COOKIE_NAME, refreshToken)))
        .andExpect(status().isOk())
        .andExpect(cookie().maxAge(AuthConstants.REFRESH_COOKIE_NAME, 0));

      refresh(refreshToken).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("is idempotent without a cookie")
    void logoutWithoutCookie() throws Exception {
      mockMvc.perform(post(LOGOUT_URL)).andExpect(status().isOk());
    }
  }

  private ResultActions register(String email, String password) throws Exception {
    return mockMvc.perform(post(REGISTER_URL)
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {"email": "%s", "password": "%s", "fullName": "Test User"}
        """.formatted(email, password)));
  }

  private ResultActions login(String email, String password) throws Exception {
    return mockMvc.perform(post(LOGIN_URL)
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {"email": "%s", "password": "%s"}
        """.formatted(email, password)));
  }

  private ResultActions refresh(String refreshToken) throws Exception {
    return mockMvc.perform(post(REFRESH_URL).cookie(new Cookie(AuthConstants.REFRESH_COOKIE_NAME, refreshToken)));
  }

  private static String accessToken(MockHttpServletResponse response) throws Exception {
    return JsonPath.read(response.getContentAsString(), "$.data.accessToken");
  }

  private static String refreshCookie(MockHttpServletResponse response) {
    Cookie cookie = response.getCookie(AuthConstants.REFRESH_COOKIE_NAME);
    assertThat(cookie).as("refresh cookie").isNotNull();
    return cookie.getValue();
  }

  private static String uniqueEmail() {
    return "user-" + UUID.randomUUID() + "@example.com";
  }
}
