package com.budwiser.agent.orchestrator;

import com.budwiser.agent.constant.AgentConstants;
import com.budwiser.agent.llm.LlmClient;
import com.budwiser.agent.llm.LlmMessage;
import com.budwiser.agent.llm.LlmResponse;
import com.budwiser.agent.llm.LlmToolCall;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.ToolExecution;
import com.budwiser.agent.tool.ToolExecutor;
import com.budwiser.agent.tool.ToolRegistry;
import com.budwiser.agent.tool.ToolResult;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * The agent loop (PRD §13): ask the model → run the tools it picks through the validation pipeline → feed results
 * back → repeat until it answers in plain text or the step limit is hit. Holds no DB transaction while waiting on
 * the LLM (each tool call manages its own).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentOrchestrator {
  private static final String EMPTY_REPLY = "I don't have an answer for that yet. Could you rephrase?";

  private final LlmClient llmClient;
  private final ToolRegistry toolRegistry;
  private final ToolExecutor toolExecutor;
  private final SystemPromptFactory systemPromptFactory;

  public AgentTurn run(ToolContext context, List<LlmMessage> history, String userMessage) {
    return run(context, history, userMessage, AgentEventListener.NO_OP);
  }

  public AgentTurn run(ToolContext context, List<LlmMessage> history, String userMessage, AgentEventListener listener) {
    List<LlmMessage> messages = new ArrayList<>();
    messages.add(LlmMessage.system(systemPromptFactory.build(context.userId())));
    messages.addAll(history);
    messages.add(LlmMessage.user(userMessage));

    List<ToolExecution> executions = new ArrayList<>();
    String provider = null;
    for (int step = 1; step <= AgentConstants.MAX_STEPS; step++) {
      LlmResponse response = llmClient.chat(messages, toolRegistry.definitions());
      provider = response.provider();
      if (!response.hasToolCalls()) {
        log.info("[run] turn complete, steps: {}, toolCalls: {}, provider: {}", step, executions.size(), provider);
        return new AgentTurn(replyOrDefault(response.content()), executions, step, provider);
      }

      messages.add(LlmMessage.assistantToolCalls(response.content(), response.toolCalls()));
      List<LlmToolCall> calls = response.toolCalls();
      for (int i = 0; i < calls.size(); i++) {
        LlmToolCall call = calls.get(i);
        // Every tool_call id must get a tool message back, so excess calls are answered with a rejection.
        ToolResult result;
        if (i < AgentConstants.MAX_TOOL_CALLS_PER_STEP) {
          listener.onToolCall(call);
          ToolExecution execution = toolExecutor.execute(call, context);
          executions.add(execution);
          listener.onToolResult(execution);
          result = execution.result();
        } else {
          result = ToolResult.rejected("Too many tool calls in one step; call at most "
            + AgentConstants.MAX_TOOL_CALLS_PER_STEP + " at a time.");
        }
        messages.add(LlmMessage.tool(call.id(), toolExecutor.serializeForModel(result)));
      }
    }
    log.warn("[run] step limit reached, steps: {}, toolCalls: {}", AgentConstants.MAX_STEPS, executions.size());
    return new AgentTurn(AgentConstants.STEP_LIMIT_REPLY, executions, AgentConstants.MAX_STEPS, provider);
  }

  private static String replyOrDefault(String content) {
    return StringUtils.hasText(content) ? content.trim() : EMPTY_REPLY;
  }
}
