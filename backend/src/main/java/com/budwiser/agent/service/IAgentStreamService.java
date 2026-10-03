package com.budwiser.agent.service;

import com.budwiser.agent.dto.AgentChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IAgentStreamService {

  /**
   * Runs the agent turn asynchronously and streams Server-Sent Events:
   * conversation → (tool_call → tool_result → action_pending?)* → done (full response) | error.
   */
  SseEmitter stream(AgentChatRequest request);
}
