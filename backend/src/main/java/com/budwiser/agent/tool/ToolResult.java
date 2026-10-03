package com.budwiser.agent.tool;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * What the model sees after a tool call (serialized as JSON in the tool message).
 *
 * @param actionId set for PENDING_CONFIRMATION — the id the user confirms
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ToolResult(ToolStatus status, Object data, String message, String actionId) {

  public static ToolResult success(Object data) {
    return new ToolResult(ToolStatus.SUCCESS, data, null, null);
  }

  public static ToolResult rejected(String reason) {
    return new ToolResult(ToolStatus.REJECTED, null, reason, null);
  }

  public static ToolResult error(String message) {
    return new ToolResult(ToolStatus.ERROR, null, message, null);
  }

  public static ToolResult pending(String summary) {
    return new ToolResult(ToolStatus.PENDING_CONFIRMATION, null, summary, null);
  }

  public ToolResult withActionId(Long id) {
    return new ToolResult(status, data, message, String.valueOf(id));
  }
}
