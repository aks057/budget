package com.budwiser.support;

import com.budwiser.agent.llm.LlmClient;
import com.budwiser.agent.llm.LlmMessage;
import com.budwiser.agent.llm.LlmResponse;
import com.budwiser.agent.llm.LlmToolCall;
import com.budwiser.agent.llm.LlmToolDefinition;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scripted LLM for tests: each chat() consumes the next scripted step (a tool call, a reply, or a failure) and
 * records the messages it was sent, so tests can assert exactly what the model saw.
 */
public class FakeLlmClient implements LlmClient {
  public static final String PROVIDER = "fake";

  private final Deque<Object> script = new ConcurrentLinkedDeque<>();
  private final List<List<LlmMessage>> requests = new CopyOnWriteArrayList<>();
  private final AtomicInteger callIds = new AtomicInteger();

  public void reset() {
    script.clear();
    requests.clear();
  }

  public FakeLlmClient thenCallTool(String toolName, String argumentsJson) {
    return thenCallTools(new LlmToolCall("call_" + callIds.incrementAndGet(), toolName, argumentsJson));
  }

  public FakeLlmClient thenCallTools(LlmToolCall... calls) {
    script.add(new LlmResponse(null, List.of(calls), PROVIDER, PROVIDER, null, null));
    return this;
  }

  public FakeLlmClient thenReply(String text) {
    script.add(new LlmResponse(text, List.of(), PROVIDER, PROVIDER, null, null));
    return this;
  }

  public FakeLlmClient thenFail(RuntimeException failure) {
    script.add(failure);
    return this;
  }

  public List<List<LlmMessage>> requests() {
    return requests;
  }

  /** Content of the last tool-result message the model received in request {@code index}. */
  public String lastToolResultSeenAt(int index) {
    return requests.get(index).stream()
      .filter(message -> LlmMessage.TOOL.equals(message.role()))
      .reduce((first, second) -> second)
      .map(LlmMessage::content)
      .orElseThrow();
  }

  @Override
  public LlmResponse chat(List<LlmMessage> messages, List<LlmToolDefinition> tools) {
    requests.add(List.copyOf(messages));
    Object next = script.poll();
    if (next == null) {
      return new LlmResponse("(no scripted response)", List.of(), PROVIDER, PROVIDER, null, null);
    }
    if (next instanceof RuntimeException failure) {
      throw failure;
    }
    return (LlmResponse) next;
  }
}
