package com.budwiser.agent.llm;

import java.util.List;

/**
 * @param provider which provider actually answered (after fallback)
 */
public record LlmResponse(String content, List<LlmToolCall> toolCalls, String provider, String model,
                          Integer promptTokens, Integer completionTokens) {

  public boolean hasToolCalls() {
    return toolCalls != null && !toolCalls.isEmpty();
  }
}
