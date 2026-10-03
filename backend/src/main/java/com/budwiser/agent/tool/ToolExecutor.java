package com.budwiser.agent.tool;

import com.budwiser.agent.constant.ActionStatus;
import com.budwiser.agent.constant.AgentConstants;
import com.budwiser.agent.entity.AgentAction;
import com.budwiser.agent.llm.LlmToolCall;
import com.budwiser.agent.repository.IAgentActionRepository;
import com.budwiser.common.exception.BudWiserException;
import com.budwiser.common.exception.Error;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Clock;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * The validation pipeline between the model and the domain (PRD §16):
 * tool exists → arguments parse → Bean Validation → business rules → execute (read) or queue (write).
 * Every call is audited in agent_actions. Failures become structured results for the model, never HTTP 500s:
 * classifying them here is control flow, not swallowing — each one is logged and persisted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutor {
  private static final String EMPTY_ARGUMENTS = "{}";
  private static final String TRUNCATED_SUFFIX = "...(truncated)";

  private final ToolRegistry toolRegistry;
  private final IAgentActionRepository actionRepository;
  private final Validator validator;
  private final JsonMapper jsonMapper;
  private final Clock clock;

  public ToolExecution execute(LlmToolCall call, ToolContext context) {
    long startNanos = System.nanoTime();
    String arguments = StringUtils.hasText(call.argumentsJson()) ? call.argumentsJson() : EMPTY_ARGUMENTS;
    Optional<FinancialTool<?>> tool = toolRegistry.find(call.name());

    ToolResult result = tool.isPresent()
      ? run(tool.get(), arguments, context)
      : ToolResult.rejected("Unknown tool '" + call.name() + "'. Use only the tools you were given.");
    long latencyMs = (System.nanoTime() - startNanos) / 1_000_000;

    AgentAction action = audit(context, call.name(), arguments, result, latencyMs);
    if (result.status() == ToolStatus.PENDING_CONFIRMATION) {
      result = result.withActionId(action.getId());
    }
    log.info("[execute] tool call, tool: {}, status: {}, latencyMs: {}", call.name(), result.status(), latencyMs);
    return new ToolExecution(call, result, latencyMs);
  }

  /** JSON handed back to the model, size-capped so one huge result cannot blow the context window. */
  public String serializeForModel(ToolResult result) {
    return truncate(jsonMapper.writeValueAsString(result), AgentConstants.MAX_TOOL_RESULT_CHARS);
  }

  /** Parse and validate stored arguments again before executing a confirmed write (defense in depth). */
  public <A> A parseValidated(FinancialTool<A> tool, String arguments) {
    A parsed = jsonMapper.readValue(arguments, tool.argumentsType());
    Set<ConstraintViolation<A>> violations = validator.validate(parsed);
    if (!violations.isEmpty()) {
      throw new IllegalStateException("Stored arguments no longer valid for " + tool.name());
    }
    return parsed;
  }

  private <A> ToolResult run(FinancialTool<A> tool, String arguments, ToolContext context) {
    A parsed;
    try {
      parsed = jsonMapper.readValue(arguments, tool.argumentsType());
    } catch (JacksonException ex) {
      return ToolResult.rejected("Arguments for " + tool.name() + " are not valid JSON matching its schema.");
    }

    Set<ConstraintViolation<A>> violations = validator.validate(parsed);
    if (!violations.isEmpty()) {
      return ToolResult.rejected("Invalid arguments: " + violations.stream()
        .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
        .map(v -> v.getPropertyPath() + " " + v.getMessage())
        .collect(Collectors.joining("; ")));
    }

    try {
      if (tool.requiresConfirmation()) {
        return ToolResult.pending(truncate(tool.describe(parsed, context), AgentConstants.SUMMARY_MAX_LENGTH));
      }
      return ToolResult.success(tool.execute(parsed, context));
    } catch (BudWiserException ex) {
      return ToolResult.rejected(describe(ex));
    } catch (RuntimeException ex) {
      log.error("[run] tool failed unexpectedly, tool: {}", tool.name(), ex);
      return ToolResult.error("The tool failed unexpectedly. Tell the user to try again later.");
    }
  }

  private AgentAction audit(ToolContext context, String toolName, String arguments, ToolResult result, long latencyMs) {
    AgentAction action = new AgentAction();
    action.setUserId(context.userId());
    action.setConversationId(context.conversationId());
    action.setToolName(truncate(String.valueOf(toolName), 64));
    action.setArguments(arguments);
    action.setLatencyMs((int) latencyMs);
    if (result.status() == ToolStatus.PENDING_CONFIRMATION) {
      action.setStatus(ActionStatus.PENDING_CONFIRMATION);
      action.setSummary(result.message());
      action.setExpiresAt(clock.millis() + AgentConstants.PENDING_ACTION_TTL.toMillis());
    } else {
      action.setStatus(ActionStatus.valueOf(result.status().name()));
      action.setResult(truncate(jsonMapper.writeValueAsString(result), AgentConstants.MAX_AUDIT_RESULT_CHARS));
    }
    return actionRepository.save(action);
  }

  private static String describe(BudWiserException ex) {
    String details = ex.getErrors().stream()
      .map(Error::getMessage)
      .filter(message -> message != null && !message.equals(ex.getMessage()))
      .collect(Collectors.joining("; "));
    return details.isEmpty() ? ex.getMessage() : ex.getMessage() + ": " + details;
  }

  private static String truncate(String value, int max) {
    if (value == null || value.length() <= max) {
      return value;
    }
    return value.substring(0, max - TRUNCATED_SUFFIX.length()) + TRUNCATED_SUFFIX;
  }
}
