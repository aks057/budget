package com.budwiser.agent.service;

import com.budwiser.agent.dto.AgentChatRequest;
import com.budwiser.agent.dto.AgentDtos.AgentChatResponse;
import com.budwiser.agent.dto.AgentDtos.ConversationDto;
import com.budwiser.agent.dto.AgentDtos.MessageDto;
import com.budwiser.agent.dto.AgentDtos.PendingActionDto;
import com.budwiser.agent.orchestrator.AgentEventListener;
import java.util.List;

public interface IAgentService {

  AgentChatResponse chat(AgentChatRequest request);

  /** Same as {@link #chat(AgentChatRequest)}, reporting progress to {@code listener} as the turn runs. */
  AgentChatResponse chat(AgentChatRequest request, AgentEventListener listener);

  List<ConversationDto> conversations();

  List<MessageDto> messages(Long conversationId);

  /** Unexpired writes in this conversation still awaiting confirmation (so the UI can restore confirm cards). */
  List<PendingActionDto> pendingActions(Long conversationId);
}
