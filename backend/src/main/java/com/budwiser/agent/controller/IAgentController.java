package com.budwiser.agent.controller;

import com.budwiser.agent.dto.AgentChatRequest;
import com.budwiser.agent.dto.AgentDtos.ActionResultDto;
import com.budwiser.agent.dto.AgentDtos.AgentChatResponse;
import com.budwiser.agent.dto.AgentDtos.ConversationDto;
import com.budwiser.agent.dto.AgentDtos.MessageDto;
import com.budwiser.agent.dto.AgentDtos.PendingActionDto;
import com.budwiser.common.ratelimit.RateLimit;
import com.budwiser.common.ratelimit.RateLimitPolicy;
import com.budwiser.common.response.Response;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/agent")
public interface IAgentController {

  @RateLimit(RateLimitPolicy.AGENT)
  @PostMapping("/chat")
  Response<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request);

  /** Server-Sent Events version of /chat (used by the web UI for the live tool timeline). */
  @RateLimit(RateLimitPolicy.AGENT)
  @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  SseEmitter chatStream(@Valid @RequestBody AgentChatRequest request);

  /** Most recent 50. */
  @GetMapping("/conversations")
  Response<List<ConversationDto>> conversations();

  /** Most recent 100 messages, oldest first. */
  @GetMapping("/conversations/{id}/messages")
  Response<List<MessageDto>> messages(@PathVariable Long id);

  @GetMapping("/conversations/{id}/pending-actions")
  Response<List<PendingActionDto>> pendingActions(@PathVariable Long id);

  @PostMapping("/actions/{id}/confirm")
  Response<ActionResultDto> confirm(@PathVariable Long id);

  @PostMapping("/actions/{id}/reject")
  Response<ActionResultDto> reject(@PathVariable Long id);
}
