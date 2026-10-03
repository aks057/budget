package com.budwiser.support;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Thin MockMvc helper for integration tests: register users, call endpoints as a user, pull ids out of responses.
 */
public final class TestApi {
  private static final String BEARER = "Bearer ";

  private final MockMvc mockMvc;

  public TestApi(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  /** Registers a fresh user (default categories are seeded) and returns its access token. */
  public String registerUser() throws Exception {
    String body = """
      {"email": "user-%s@example.com", "password": "correct-horse-42", "fullName": "Test User"}
      """.formatted(UUID.randomUUID());
    String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON).content(body))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();
    return JsonPath.read(response, "$.data.accessToken");
  }

  public ResultActions get(String token, String url) throws Exception {
    return mockMvc.perform(authorized(MockMvcRequestBuilders.get(url), token));
  }

  public ResultActions post(String token, String url, String json) throws Exception {
    return mockMvc.perform(withJson(MockMvcRequestBuilders.post(url), token, json));
  }

  public ResultActions put(String token, String url, String json) throws Exception {
    return mockMvc.perform(withJson(MockMvcRequestBuilders.put(url), token, json));
  }

  public ResultActions delete(String token, String url) throws Exception {
    return mockMvc.perform(authorized(MockMvcRequestBuilders.delete(url), token));
  }

  /** POSTs, asserts 201, returns $.data.id. */
  public String create(String token, String url, String json) throws Exception {
    return read(post(token, url, json).andExpect(status().isCreated()), "$.data.id");
  }

  /** Id of one of the user's categories by exact name (e.g. a seeded default like "Food"). */
  public String categoryId(String token, String name) throws Exception {
    List<String> ids = read(get(token, "/api/v1/categories"), "$.data[?(@.name == '" + name + "')].id");
    if (ids.isEmpty()) {
      throw new IllegalStateException("No category named " + name);
    }
    return ids.getFirst();
  }

  public static <T> T read(ResultActions result, String jsonPath) throws Exception {
    return JsonPath.read(result.andReturn().getResponse().getContentAsString(), jsonPath);
  }

  private static MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder builder, String token, String json) {
    return authorized(builder, token).contentType(MediaType.APPLICATION_JSON).content(json);
  }

  private static MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder builder, String token) {
    return builder.header(HttpHeaders.AUTHORIZATION, BEARER + token);
  }
}
