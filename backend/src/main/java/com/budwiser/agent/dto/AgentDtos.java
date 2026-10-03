package com.budwiser.agent.dto;

import com.budwiser.agent.constant.ActionStatus;
import com.budwiser.agent.constant.MessageRole;
import com.budwiser.agent.tool.ToolStatus;
import java.util.List;

/**
 * Response shapes of the agent API (immutable read models).
 */
public final class AgentDtos {
  private AgentDtos() {}

  /**
   * @param toolCalls      what the agent did (transparency: shown as a timeline in the UI)
   * @param pendingActions writes waiting for the user's confirmation
   */
  public record AgentChatResponse(String conversationId, String reply, List<ToolCallDto> toolCalls,
                                  List<PendingActionDto> pendingActions) {}

  public record ToolCallDto(String name, ToolStatus status, long latencyMs) {}

  public record PendingActionDto(String actionId, String toolName, String summary) {}

  public record ConversationDto(String id, String title, long lastMessageAt) {}

  public record MessageDto(String id, MessageRole role, String content, long createdAt) {}

  public record ActionResultDto(String actionId, ActionStatus status, String message, Object data) {}
}
