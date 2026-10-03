package com.budwiser.auth;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budwiser.AbstractIntegrationTest;
import com.budwiser.support.TestApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class UserProfileIT extends AbstractIntegrationTest {
  private static final String ME = "/api/v1/users/me";

  @Autowired
  private MockMvc mockMvc;
  private TestApi api;
  private String token;

  @BeforeEach
  void setUp() throws Exception {
    api = new TestApi(mockMvc);
    token = api.registerUser();
  }

  @Test
  @DisplayName("PUT /users/me updates name and currency for the caller only")
  void updateProfile() throws Exception {
    api.put(token, ME, """
        {"fullName": "  Abhi Kumar ", "currency": "USD"}
        """)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.fullName").value("Abhi Kumar"))
      .andExpect(jsonPath("$.data.currency").value("USD"));
    api.get(token, ME).andExpect(jsonPath("$.data.currency").value("USD"));

    String otherToken = api.registerUser();
    api.get(otherToken, ME).andExpect(jsonPath("$.data.currency").value("INR"));
  }

  @Test
  @DisplayName("unsupported currencies and blank names are rejected")
  void validation() throws Exception {
    api.put(token, ME, """
        {"fullName": "", "currency": "JPY"}
        """)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.length()").value(2));
  }
}
