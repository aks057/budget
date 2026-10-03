package com.budwiser.agent.llm;

import java.util.List;

/**
 * One chat message in OpenAI chat-completions shape (system / user / assistant / tool).
 *
 * @param toolCalls  assistant messages only: the tools the model asked to call
 * @param toolCallId tool messages only: which call this result answers
 */
public record LlmMessage(String role, String content, List<LlmToolCall> toolCalls, String toolCallId) {
  public static final String SYSTEM = "system";
  public static final String USER = "user";
  public static final String ASSISTANT = "assistant";
  public static final String TOOL = "tool";

  public static LlmMessage system(String content) {
    return new LlmMessage(SYSTEM, content, List.of(), null);
  }

  public static LlmMessage user(String content) {
    return new LlmMessage(USER, content, List.of(), null);
  }

  public static LlmMessage assistant(String content) {
    return new LlmMessage(ASSISTANT, content, List.of(), null);
  }

  public static LlmMessage assistantToolCalls(String content, List<LlmToolCall> toolCalls) {
    return new LlmMessage(ASSISTANT, content, List.copyOf(toolCalls), null);
  }

  public static LlmMessage tool(String toolCallId, String content) {
    return new LlmMessage(TOOL, content, List.of(), toolCallId);
  }
}
