package com.budwiser.agent.llm;

import java.util.List;

/**
 * Provider-agnostic chat completion with tool calling. Implementations: one per OpenAI-compatible provider
 * (Groq, Hugging Face router), a fallback chain over them, and a scripted fake for tests.
 */
public interface LlmClient {

  LlmResponse chat(List<LlmMessage> messages, List<LlmToolDefinition> tools);
}
