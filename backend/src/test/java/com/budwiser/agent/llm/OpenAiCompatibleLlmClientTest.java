package com.budwiser.agent.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

class OpenAiCompatibleLlmClientTest {
  private static final String URL = "https://llm.test/v1/chat/completions";

  private final JsonMapper jsonMapper = JsonMapper.builder().build();
  private MockRestServiceServer server;
  private OpenAiCompatibleLlmClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder().baseUrl("https://llm.test/v1");
    server = MockRestServiceServer.bindTo(builder).build();
    LlmProperties.Provider provider = new LlmProperties.Provider("test", "https://llm.test/v1", "test-model", "test-key");
    LlmProperties properties = new LlmProperties(Duration.ofSeconds(5), 512, 0.2,
      new LlmProperties.CircuitBreakerSettings(3, Duration.ofMinutes(1)), List.of(provider));
    client = new OpenAiCompatibleLlmClient(provider, properties, builder.build(), jsonMapper);
  }

  @Test
  @DisplayName("sends an OpenAI-format request with tools and parses tool calls (string arguments)")
  void toolCallRoundTrip() {
    server.expect(requestTo(URL))
      .andExpect(method(HttpMethod.POST))
      .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
      .andExpect(jsonPath("$.model").value("test-model"))
      .andExpect(jsonPath("$.messages[0].role").value("system"))
      .andExpect(jsonPath("$.messages[1].content").value("How much did I spend?"))
      .andExpect(jsonPath("$.tools[0].type").value("function"))
      .andExpect(jsonPath("$.tools[0].function.name").value("get_monthly_summary"))
      .andExpect(jsonPath("$.tool_choice").value("auto"))
      .andRespond(withSuccess("""
        {"choices": [{"message": {"role": "assistant", "content": null, "tool_calls": [
          {"id": "call_abc", "type": "function",
           "function": {"name": "get_monthly_summary", "arguments": "{\\"month\\":\\"2026-10\\"}"}}]}}],
         "usage": {"prompt_tokens": 120, "completion_tokens": 15}}
        """, MediaType.APPLICATION_JSON));

    LlmResponse actual = client.chat(
      List.of(LlmMessage.system("rules"), LlmMessage.user("How much did I spend?")),
      List.of(new LlmToolDefinition("get_monthly_summary", "summary", jsonMapper.readTree("{\"type\":\"object\"}"))));

    assertThat(actual.hasToolCalls()).isTrue();
    assertThat(actual.toolCalls().getFirst().id()).isEqualTo("call_abc");
    assertThat(actual.toolCalls().getFirst().argumentsJson()).isEqualTo("{\"month\":\"2026-10\"}");
    assertThat(actual.promptTokens()).isEqualTo(120);
    assertThat(actual.provider()).isEqualTo("test");
    server.verify();
  }

  @Test
  @DisplayName("tool-call history is serialized back with ids, and object-shaped arguments are tolerated")
  void historySerializationAndObjectArguments() {
    server.expect(requestTo(URL))
      .andExpect(jsonPath("$.messages[1].tool_calls[0].id").value("call_1"))
      .andExpect(jsonPath("$.messages[1].tool_calls[0].function.arguments").value("{}"))
      .andExpect(jsonPath("$.messages[2].role").value("tool"))
      .andExpect(jsonPath("$.messages[2].tool_call_id").value("call_1"))
      .andRespond(withSuccess("""
        {"choices": [{"message": {"tool_calls": [
          {"function": {"name": "get_budgets", "arguments": {"month": "2026-09"}}}]}}]}
        """, MediaType.APPLICATION_JSON));

    LlmResponse actual = client.chat(List.of(
      LlmMessage.user("budgets?"),
      LlmMessage.assistantToolCalls(null, List.of(new LlmToolCall("call_1", "get_goals", "{}"))),
      LlmMessage.tool("call_1", "{\"status\":\"SUCCESS\"}")), List.of());

    assertThat(actual.toolCalls().getFirst().id()).isEqualTo("call_0");
    assertThat(actual.toolCalls().getFirst().argumentsJson()).isEqualTo("{\"month\":\"2026-09\"}");
  }

  @Test
  @DisplayName("plain text answers have no tool calls")
  void textAnswer() {
    server.expect(requestTo(URL)).andRespond(withSuccess("""
      {"choices": [{"message": {"role": "assistant", "content": "You saved ₹12,000."}}]}
      """, MediaType.APPLICATION_JSON));

    LlmResponse actual = client.chat(List.of(LlmMessage.user("hi")), List.of());

    assertThat(actual.hasToolCalls()).isFalse();
    assertThat(actual.content()).isEqualTo("You saved ₹12,000.");
  }

  @Test
  @DisplayName("rate limits, server errors and malformed bodies become LlmProviderException (fallback trigger)")
  void failures() {
    server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
    assertThatThrownBy(() -> client.chat(List.of(LlmMessage.user("hi")), List.of()))
      .isInstanceOf(LlmProviderException.class).hasMessageContaining("429");

    server.reset();
    server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
    assertThatThrownBy(() -> client.chat(List.of(LlmMessage.user("hi")), List.of()))
      .isInstanceOf(LlmProviderException.class).hasMessageContaining("502");

    server.reset();
    server.expect(requestTo(URL)).andRespond(withSuccess("<html>oops</html>", MediaType.TEXT_HTML));
    assertThatThrownBy(() -> client.chat(List.of(LlmMessage.user("hi")), List.of()))
      .isInstanceOf(LlmProviderException.class).hasMessageContaining("unparseable");
  }
}
