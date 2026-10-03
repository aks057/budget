package com.budwiser.agent.llm;

/**
 * @param argumentsJson raw JSON string produced by the model — untrusted until parsed and validated
 */
public record LlmToolCall(String id, String name, String argumentsJson) {}
