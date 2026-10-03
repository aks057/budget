package com.budwiser.agent.controller.impl;

import com.budwiser.agent.controller.IAgentController;
import com.budwiser.agent.dto.AgentChatRequest;
import com.budwiser.agent.dto.AgentDtos.ActionResultDto;
import com.budwiser.agent.dto.AgentDtos.AgentChatResponse;
import com.budwiser.agent.dto.AgentDtos.ConversationDto;
import com.budwiser.agent.dto.AgentDtos.MessageDto;
import com.budwiser.agent.dto.AgentDtos.PendingActionDto;
import com.budwiser.agent.service.IAgentActionService;
import com.budwiser.agent.service.IAgentService;
import com.budwiser.agent.service.IAgentStreamService;
import com.budwiser.common.response.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class AgentController implements IAgentController {
  private final IAgentService agentService;
  private final IAgentActionService agentActionService;
  private final IAgentStreamService agentStreamService;

  @Override
  public Response<AgentChatResponse> chat(AgentChatRequest request) {
    return Response.<AgentChatResponse>builder().data(agentService.chat(request)).build();
  }

  @Override
  public SseEmitter chatStream(AgentChatRequest request) {
    return agentStreamService.stream(request);
  }

  @Override
  public Response<List<ConversationDto>> conversations() {
    return Response.<List<ConversationDto>>builder().data(agentService.conversations()).build();
  }

  @Override
  public Response<List<MessageDto>> messages(Long id) {
    return Response.<List<MessageDto>>builder().data(agentService.messages(id)).build();
  }

  @Override
  public Response<List<PendingActionDto>> pendingActions(Long id) {
    return Response.<List<PendingActionDto>>builder().data(agentService.pendingActions(id)).build();
  }

  @Override
  public Response<ActionResultDto> confirm(Long id) {
    return Response.<ActionResultDto>builder().data(agentActionService.confirm(id)).build();
  }

  @Override
  public Response<ActionResultDto> reject(Long id) {
    return Response.<ActionResultDto>builder().data(agentActionService.reject(id)).build();
  }
}
