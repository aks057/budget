package com.budwiser.agent.service.impl;

import com.budwiser.agent.config.AgentAsyncConfig;
import com.budwiser.agent.dto.AgentChatRequest;
import com.budwiser.agent.dto.AgentDtos.AgentChatResponse;
import com.budwiser.agent.dto.AgentDtos.PendingActionDto;
import com.budwiser.agent.llm.LlmToolCall;
import com.budwiser.agent.orchestrator.AgentEventListener;
import com.budwiser.agent.service.IAgentService;
import com.budwiser.agent.service.IAgentStreamService;
import com.budwiser.agent.tool.ToolExecution;
import com.budwiser.agent.tool.ToolStatus;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.BudWiserException;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE transport over the same agent turn as the blocking endpoint. Streams the tool timeline as it happens, then the
 * full response. If the client disconnects, the turn still finishes (pending actions and history stay consistent);
 * further sends are skipped.
 */
@Slf4j
@Service
public class AgentStreamService implements IAgentStreamService {
  public static final String EVENT_CONVERSATION = "conversation";
  public static final String EVENT_TOOL_CALL = "tool_call";
  public static final String EVENT_TOOL_RESULT = "tool_result";
  public static final String EVENT_ACTION_PENDING = "action_pending";
  public static final String EVENT_DONE = "done";
  public static final String EVENT_ERROR = "error";
  private static final Duration EMITTER_TIMEOUT = Duration.ofMinutes(2);

  private final IAgentService agentService;
  private final TaskExecutor agentExecutor;

  public AgentStreamService(IAgentService agentService,
                            @Qualifier(AgentAsyncConfig.AGENT_EXECUTOR) TaskExecutor agentExecutor) {
    this.agentService = agentService;
    this.agentExecutor = agentExecutor;
  }

  @Override
  public SseEmitter stream(AgentChatRequest request) {
    SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT.toMillis()) {
      @Override
      protected void extendResponse(ServerHttpResponse outputMessage) {
        super.extendResponse(outputMessage);
        // Proxies (Next.js rewrites' gzip, nginx) must forward each event as it is written, not buffer the stream.
        outputMessage.getHeaders().set(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform");
        outputMessage.getHeaders().set("X-Accel-Buffering", "no");
      }
    };
    EventSink sink = new EventSink(emitter);
    emitter.onTimeout(() -> log.warn("[stream] SSE emitter timed out"));
    try {
      agentExecutor.execute(() -> runTurn(request, sink));
    } catch (TaskRejectedException ex) {
      log.warn("[stream] agent executor saturated, rejecting turn");
      sink.error(ErrorCode.AI_UNAVAILABLE.getCode(), ErrorCode.AI_UNAVAILABLE.getDescription());
    }
    return emitter;
  }

  private void runTurn(AgentChatRequest request, EventSink sink) {
    try {
      AgentChatResponse response = agentService.chat(request, new AgentEventListener() {
        @Override
        public void onConversation(Long conversationId) {
          sink.send(EVENT_CONVERSATION, Map.of("conversationId", String.valueOf(conversationId)));
        }

        @Override
        public void onToolCall(LlmToolCall call) {
          sink.send(EVENT_TOOL_CALL, Map.of("name", String.valueOf(call.name())));
        }

        @Override
        public void onToolResult(ToolExecution execution) {
          sink.send(EVENT_TOOL_RESULT, Map.of(
            "name", String.valueOf(execution.call().name()),
            "status", execution.result().status(),
            "latencyMs", execution.latencyMs()));
          if (execution.result().status() == ToolStatus.PENDING_CONFIRMATION) {
            sink.send(EVENT_ACTION_PENDING, new PendingActionDto(execution.result().actionId(),
              execution.call().name(), execution.result().message()));
          }
        }
      });
      sink.done(response);
    } catch (BudWiserException ex) {
      sink.error(ex.getErrors().isEmpty() ? ErrorCode.INTERNAL_ERROR.getCode() : ex.getErrors().getFirst().getCode(),
        ex.getMessage());
    } catch (RuntimeException ex) {
      log.error("[runTurn] streaming agent turn failed", ex);
      sink.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getDescription());
    }
  }

  /** Serializes sends and stops writing once the client is gone. */
  private static final class EventSink {
    private final SseEmitter emitter;
    private final AtomicBoolean open = new AtomicBoolean(true);

    private EventSink(SseEmitter emitter) {
      this.emitter = emitter;
    }

    synchronized void send(String event, Object data) {
      if (!open.get()) {
        return;
      }
      try {
        emitter.send(SseEmitter.event().name(event).data(data, MediaType.APPLICATION_JSON));
      } catch (IOException | IllegalStateException ex) {
        // Client disconnected: classification, not swallowing — the turn itself continues and is persisted.
        open.set(false);
        log.info("[send] SSE client disconnected, event: {}", event);
      }
    }

    synchronized void done(Object response) {
      send(EVENT_DONE, response);
      complete();
    }

    synchronized void error(String code, String message) {
      send(EVENT_ERROR, Map.of("code", code, "message", message));
      complete();
    }

    private void complete() {
      if (open.getAndSet(false)) {
        emitter.complete();
      }
    }
  }
}
