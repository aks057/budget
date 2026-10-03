package com.budwiser.agent.constant;

import java.time.Duration;

public final class AgentConstants {
  private AgentConstants() {}

  /** Max LLM round-trips per user message (bounds cost and runaway tool loops). */
  public static final int MAX_STEPS = 6;
  /** Max tool calls honoured from a single model response. */
  public static final int MAX_TOOL_CALLS_PER_STEP = 5;
  /** Conversation memory replayed to the model. */
  public static final int HISTORY_MESSAGES = 20;
  /** Tool results larger than this are truncated before going back to the model. */
  public static final int MAX_TOOL_RESULT_CHARS = 8_000;
  /** Audit rows keep at most this much of a result. */
  public static final int MAX_AUDIT_RESULT_CHARS = 4_000;
  public static final int MAX_CONVERSATIONS_LISTED = 50;
  public static final int MAX_MESSAGES_LISTED = 100;
  public static final int TITLE_MAX_LENGTH = 60;
  public static final int SUMMARY_MAX_LENGTH = 500;
  /** A proposed write must be confirmed within this window. */
  public static final Duration PENDING_ACTION_TTL = Duration.ofMinutes(15);

  public static final String STEP_LIMIT_REPLY =
    "I couldn't finish that within my step limit. Could you ask a narrower question?";
}
