package com.budwiser.agent.service.impl;

import com.budwiser.agent.constant.AgentConstants;
import com.budwiser.agent.constant.MessageRole;
import com.budwiser.agent.dto.AgentChatRequest;
import com.budwiser.agent.dto.AgentDtos.AgentChatResponse;
import com.budwiser.agent.dto.AgentDtos.ConversationDto;
import com.budwiser.agent.dto.AgentDtos.MessageDto;
import com.budwiser.agent.dto.AgentDtos.PendingActionDto;
import com.budwiser.agent.dto.AgentDtos.ToolCallDto;
import com.budwiser.agent.entity.AgentConversation;
import com.budwiser.agent.entity.AgentMessage;
import com.budwiser.agent.llm.LlmMessage;
import com.budwiser.agent.orchestrator.AgentEventListener;
import com.budwiser.agent.orchestrator.AgentOrchestrator;
import com.budwiser.agent.orchestrator.AgentTurn;
import com.budwiser.agent.constant.ActionStatus;
import com.budwiser.agent.repository.IAgentActionRepository;
import com.budwiser.agent.repository.IAgentConversationRepository;
import com.budwiser.agent.repository.IAgentMessageRepository;
import com.budwiser.agent.service.IAgentService;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.ToolStatus;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.ResourceNotFoundException;
import com.budwiser.security.service.ICurrentUserProvider;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deliberately not @Transactional around chat(): an LLM round-trip can take seconds and must not hold a DB
 * connection/transaction. Persistence happens in short, independent writes before and after the agent turn.
 */
@Service
@RequiredArgsConstructor
public class AgentService implements IAgentService {
  private final AgentOrchestrator orchestrator;
  private final IAgentConversationRepository conversationRepository;
  private final IAgentMessageRepository messageRepository;
  private final IAgentActionRepository actionRepository;
  private final ICurrentUserProvider currentUserProvider;
  private final Clock clock;

  @Override
  public AgentChatResponse chat(AgentChatRequest request) {
    return chat(request, AgentEventListener.NO_OP);
  }

  @Override
  public AgentChatResponse chat(AgentChatRequest request, AgentEventListener listener) {
    Long userId = currentUserProvider.getUserId();
    String userMessage = request.getMessage().trim();
    AgentConversation conversation = request.getConversationId() == null
      ? startConversation(userId, userMessage)
      : getOwned(userId, request.getConversationId());
    listener.onConversation(conversation.getId());
    List<LlmMessage> history = history(conversation.getId(), userId);
    conversation = saveMessage(conversation, MessageRole.USER, userMessage);

    AgentTurn turn = orchestrator.run(new ToolContext(userId, conversation.getId()), history, userMessage, listener);

    conversation = saveMessage(conversation, MessageRole.ASSISTANT, turn.reply());
    return new AgentChatResponse(
      String.valueOf(conversation.getId()),
      turn.reply(),
      turn.executions().stream()
        .map(execution -> new ToolCallDto(execution.call().name(), execution.result().status(), execution.latencyMs()))
        .toList(),
      turn.executions().stream()
        .filter(execution -> execution.result().status() == ToolStatus.PENDING_CONFIRMATION)
        .map(execution -> new PendingActionDto(execution.result().actionId(), execution.call().name(),
          execution.result().message()))
        .toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<ConversationDto> conversations() {
    return conversationRepository.findByUserIdOrderByLastMessageAtDesc(currentUserProvider.getUserId(),
        PageRequest.of(0, AgentConstants.MAX_CONVERSATIONS_LISTED)).stream()
      .map(c -> new ConversationDto(String.valueOf(c.getId()), c.getTitle(), c.getLastMessageAt()))
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MessageDto> messages(Long conversationId) {
    Long userId = currentUserProvider.getUserId();
    getOwned(userId, conversationId);
    List<AgentMessage> recent = new ArrayList<>(
      messageRepository.findRecent(conversationId, userId, AgentConstants.MAX_MESSAGES_LISTED));
    Collections.reverse(recent);
    return recent.stream()
      .map(m -> new MessageDto(String.valueOf(m.getId()), m.getRole(), m.getContent(), m.getCreatedAt()))
      .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PendingActionDto> pendingActions(Long conversationId) {
    Long userId = currentUserProvider.getUserId();
    getOwned(userId, conversationId);
    long now = clock.millis();
    return actionRepository.findByUserIdAndConversationIdAndStatusOrderByIdAsc(userId, conversationId,
        ActionStatus.PENDING_CONFIRMATION).stream()
      // Expired proposals can no longer be confirmed, so they are not offered.
      .filter(action -> action.getExpiresAt() == null || action.getExpiresAt() >= now)
      .map(action -> new PendingActionDto(String.valueOf(action.getId()), action.getToolName(), action.getSummary()))
      .toList();
  }

  private AgentConversation startConversation(Long userId, String firstMessage) {
    AgentConversation conversation = new AgentConversation();
    conversation.setUserId(userId);
    conversation.setTitle(firstMessage.length() <= AgentConstants.TITLE_MAX_LENGTH
      ? firstMessage : firstMessage.substring(0, AgentConstants.TITLE_MAX_LENGTH - 1) + "…");
    conversation.setLastMessageAt(clock.millis());
    return conversationRepository.save(conversation);
  }

  private AgentConversation getOwned(Long userId, Long conversationId) {
    return conversationRepository.findByIdAndUserId(conversationId, userId)
      .orElseThrow(() -> new ResourceNotFoundException(conversationId, ErrorCode.CONVERSATION_NOT_FOUND));
  }

  /** Replayed memory: plain user/assistant turns only, oldest first. */
  private List<LlmMessage> history(Long conversationId, Long userId) {
    List<AgentMessage> recent = new ArrayList<>(
      messageRepository.findRecent(conversationId, userId, AgentConstants.HISTORY_MESSAGES));
    Collections.reverse(recent);
    return recent.stream()
      .map(m -> m.getRole() == MessageRole.USER ? LlmMessage.user(m.getContent()) : LlmMessage.assistant(m.getContent()))
      .toList();
  }

  /**
   * Returns the merged conversation: save() on a detached entity yields a NEW managed copy carrying the bumped
   * @Version — keeping the old reference would make the next save look stale (optimistic-lock failure).
   */
  private AgentConversation saveMessage(AgentConversation conversation, MessageRole role, String content) {
    AgentMessage message = new AgentMessage();
    message.setConversationId(conversation.getId());
    message.setUserId(conversation.getUserId());
    message.setRole(role);
    message.setContent(content);
    messageRepository.save(message);
    conversation.setLastMessageAt(clock.millis());
    return conversationRepository.save(conversation);
  }
}
