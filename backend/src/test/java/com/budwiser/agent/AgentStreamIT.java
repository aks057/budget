package com.budwiser.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budwiser.AbstractIntegrationTest;
import com.budwiser.common.exception.AiUnavailableException;
import com.budwiser.common.ratelimit.RateLimitInterceptor;
import com.budwiser.support.FakeLlmClient;
import com.budwiser.support.TestApi;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class AgentStreamIT extends AbstractIntegrationTest {
  private static final String STREAM = "/api/v1/agent/chat/stream";
  private static final long ASYNC_TIMEOUT_MS = 10_000;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private FakeLlmClient fakeLlm;
  private TestApi api;
  private String token;
  private MockHttpServletResponse lastResponse;

  @BeforeEach
  void setUp() throws Exception {
    fakeLlm.reset();
    api = new TestApi(mockMvc);
    token = api.registerUser();
  }

  @Test
  @DisplayName("SSE streams the tool timeline in order, a pending action, then the full response")
  void streamsTimeline() throws Exception {
    fakeLlm.thenCallTool("get_budgets", "{}")
      .thenCallTool("create_budget", "{\"categoryName\": \"Shopping\", \"amount\": 7000}")
      .thenReply("Prepared a ₹7,000 Shopping budget — please confirm.");

    String events = stream("Cap my shopping at 7000");

    assertThat(events).containsSubsequence(
      "event:conversation",
      "event:tool_call", "\"name\":\"get_budgets\"",
      "event:tool_result", "\"status\":\"SUCCESS\"",
      "event:tool_call", "\"name\":\"create_budget\"",
      "event:tool_result", "\"status\":\"PENDING_CONFIRMATION\"",
      "event:action_pending", "Create a monthly budget of ₹7,000.00 for Shopping",
      "event:done", "please confirm");
    assertThat(events).doesNotContain("event:error");
    // Proxies must not buffer or compress the stream.
    assertThat(lastResponse.getHeader(HttpHeaders.CACHE_CONTROL)).contains("no-transform");
    assertThat(lastResponse.getHeader("X-Accel-Buffering")).isEqualTo("no");
  }

  @Test
  @DisplayName("an LLM outage arrives as an SSE error event, not a broken stream")
  void streamsError() throws Exception {
    fakeLlm.thenFail(new AiUnavailableException());

    String events = stream("hello");

    assertThat(events).contains("event:error").contains("BW-5001").doesNotContain("event:done");
  }

  @Test
  @DisplayName("rate limit: the 11th agent request within a minute is a 429 with Retry-After")
  void rateLimited() throws Exception {
    for (int i = 0; i < 10; i++) {
      api.post(token, "/api/v1/agent/chat", "{\"message\": \"hi " + i + "\"}")
        .andExpect(status().isOk())
        .andExpect(header().exists(RateLimitInterceptor.REMAINING_HEADER));
    }

    api.post(token, "/api/v1/agent/chat", "{\"message\": \"one too many\"}")
      .andExpect(status().isTooManyRequests())
      .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
      .andExpect(jsonPath("$.errors[0].code").value("BW-9007"));
    // The limit is per user: someone else is unaffected.
    api.post(api.registerUser(), "/api/v1/agent/chat", "{\"message\": \"hi\"}").andExpect(status().isOk());
  }

  private String stream(String message) throws Exception {
    MvcResult started = mockMvc.perform(post(STREAM)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.TEXT_EVENT_STREAM)
        .content("{\"message\": \"" + message + "\"}"))
      .andExpect(request().asyncStarted())
      .andReturn();
    started.getAsyncResult(ASYNC_TIMEOUT_MS);
    lastResponse = started.getResponse();
    return lastResponse.getContentAsString(StandardCharsets.UTF_8);
  }
}
