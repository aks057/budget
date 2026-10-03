package com.budwiser.agent.orchestrator;

import com.budwiser.agent.llm.LlmToolCall;
import com.budwiser.agent.tool.ToolExecution;

/**
 * Progress callbacks from the agent loop — lets the SSE endpoint show a live tool timeline
 * ("Checking your budgets…") while the turn is still running.
 */
public interface AgentEventListener {

  AgentEventListener NO_OP = new AgentEventListener() {};

  default void onConversation(Long conversationId) {}

  default void onToolCall(LlmToolCall call) {}

  default void onToolResult(ToolExecution execution) {}
}
