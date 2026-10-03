package com.budwiser.agent.llm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Chat-completions client for any OpenAI-compatible endpoint (Groq, Hugging Face router, OpenRouter, ...).
 * Bodies are built and parsed as JSON trees here, so the wire format is explicit and independent of
 * HTTP-converter auto-configuration. Request/response bodies are never logged (they contain financial data).
 */
@Slf4j
public class OpenAiCompatibleLlmClient implements LlmClient {
  private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
  private static final String BEARER = "Bearer ";
  private static final String TYPE_FUNCTION = "function";
  private static final int ERROR_STATUS_THRESHOLD = 400;

  private final LlmProperties.Provider provider;
  private final LlmProperties properties;
  private final RestClient restClient;
  private final JsonMapper jsonMapper;

  public OpenAiCompatibleLlmClient(LlmProperties.Provider provider, LlmProperties properties, RestClient restClient,
                                   JsonMapper jsonMapper) {
    this.provider = provider;
    this.properties = properties;
    this.restClient = restClient;
    this.jsonMapper = jsonMapper;
  }

  public String providerName() {
    return provider.name();
  }

  @Override
  public LlmResponse chat(List<LlmMessage> messages, List<LlmToolDefinition> tools) {
    String requestBody = jsonMapper.writeValueAsString(buildRequest(messages, tools));
    String responseBody;
    try {
      responseBody = restClient.post()
        .uri(CHAT_COMPLETIONS_PATH)
        .header(HttpHeaders.AUTHORIZATION, BEARER + provider.apiKey())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(requestBody)
        .exchange((request, response) -> {
          int status = response.getStatusCode().value();
          if (status >= ERROR_STATUS_THRESHOLD) {
            throw new LlmProviderException(provider.name() + " returned HTTP " + status);
          }
          return readBody(response.getBody().readAllBytes());
        });
    } catch (RestClientException ex) {
      // Timeouts and connection failures surface here.
      throw new LlmProviderException(provider.name() + " request failed: " + ex.getClass().getSimpleName(), ex);
    }
    try {
      return parseResponse(jsonMapper.readTree(responseBody));
    } catch (JacksonException ex) {
      throw new LlmProviderException(provider.name() + " returned unparseable JSON", ex);
    }
  }

  private ObjectNode buildRequest(List<LlmMessage> messages, List<LlmToolDefinition> tools) {
    ObjectNode root = jsonMapper.createObjectNode();
    root.put("model", provider.model());
    root.put("temperature", properties.temperature());
    root.put("max_tokens", properties.maxTokens());
    ArrayNode messageArray = root.putArray("messages");
    messages.forEach(message -> messageArray.add(toJson(message)));
    if (!tools.isEmpty()) {
      ArrayNode toolArray = root.putArray("tools");
      for (LlmToolDefinition tool : tools) {
        ObjectNode function = toolArray.addObject().put("type", TYPE_FUNCTION).putObject(TYPE_FUNCTION);
        function.put("name", tool.name());
        function.put("description", tool.description());
        function.set("parameters", tool.parameters());
      }
      root.put("tool_choice", "auto");
    }
    return root;
  }

  private ObjectNode toJson(LlmMessage message) {
    ObjectNode node = jsonMapper.createObjectNode();
    node.put("role", message.role());
    if (message.content() == null) {
      node.putNull("content");
    } else {
      node.put("content", message.content());
    }
    if (message.toolCallId() != null) {
      node.put("tool_call_id", message.toolCallId());
    }
    if (message.toolCalls() != null && !message.toolCalls().isEmpty()) {
      ArrayNode calls = node.putArray("tool_calls");
      for (LlmToolCall call : message.toolCalls()) {
        ObjectNode callNode = calls.addObject().put("id", call.id()).put("type", TYPE_FUNCTION);
        callNode.putObject(TYPE_FUNCTION).put("name", call.name()).put("arguments", call.argumentsJson());
      }
    }
    return node;
  }

  private LlmResponse parseResponse(JsonNode root) {
    JsonNode message = root.path("choices").path(0).path("message");
    if (message.isMissingNode()) {
      throw new LlmProviderException(provider.name() + " response has no choices");
    }
    List<LlmToolCall> toolCalls = new ArrayList<>();
    JsonNode calls = message.path("tool_calls");
    for (int i = 0; i < calls.size(); i++) {
      JsonNode call = calls.path(i);
      JsonNode function = call.path(TYPE_FUNCTION);
      JsonNode arguments = function.path("arguments");
      String id = stringOrNull(call.path("id"));
      toolCalls.add(new LlmToolCall(
        id != null ? id : "call_" + i,
        stringOrNull(function.path("name")),
        // Most providers send arguments as a JSON string; some send an object.
        arguments.isString() ? arguments.stringValue() : arguments.isMissingNode() ? "{}" : arguments.toString()));
    }
    JsonNode usage = root.path("usage");
    return new LlmResponse(stringOrNull(message.path("content")), toolCalls, provider.name(), provider.model(),
      intOrNull(usage.path("prompt_tokens")), intOrNull(usage.path("completion_tokens")));
  }

  private static String readBody(byte[] bytes) throws IOException {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static String stringOrNull(JsonNode node) {
    return node.isString() ? node.stringValue() : null;
  }

  private static Integer intOrNull(JsonNode node) {
    return node.isNumber() ? node.intValue() : null;
  }
}
