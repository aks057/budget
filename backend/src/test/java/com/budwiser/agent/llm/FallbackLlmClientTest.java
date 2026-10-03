package com.budwiser.agent.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budwiser.agent.llm.FallbackLlmClient.ProviderSlot;
import com.budwiser.common.exception.AiUnavailableException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FallbackLlmClientTest {
  private static final List<LlmMessage> MESSAGES = List.of(LlmMessage.user("hi"));
  private static final LlmResponse HF_ANSWER = new LlmResponse("from hf", List.of(), "huggingface", "m", null, null);

  private OpenAiCompatibleLlmClient mockGroq;
  private OpenAiCompatibleLlmClient mockHuggingFace;
  private CircuitBreaker groqBreaker;
  private FallbackLlmClient client;

  @BeforeEach
  void setUp() {
    mockGroq = mock(OpenAiCompatibleLlmClient.class);
    mockHuggingFace = mock(OpenAiCompatibleLlmClient.class);
    when(mockGroq.providerName()).thenReturn("groq");
    when(mockHuggingFace.providerName()).thenReturn("huggingface");
    groqBreaker = new CircuitBreaker(2, Duration.ofMinutes(1), Clock.systemUTC());
    client = new FallbackLlmClient(List.of(
      new ProviderSlot(mockGroq, groqBreaker),
      new ProviderSlot(mockHuggingFace, new CircuitBreaker(2, Duration.ofMinutes(1), Clock.systemUTC()))));
  }

  @Test
  @DisplayName("primary answers → secondary is never called")
  void primaryWins() {
    LlmResponse expected = new LlmResponse("ok", List.of(), "groq", "m", null, null);
    when(mockGroq.chat(anyList(), anyList())).thenReturn(expected);

    assertThat(client.chat(MESSAGES, List.of())).isSameAs(expected);
    verify(mockHuggingFace, never()).chat(anyList(), anyList());
  }

  @Test
  @DisplayName("primary rate-limited → falls back to the secondary")
  void fallsBack() {
    when(mockGroq.chat(anyList(), anyList())).thenThrow(new LlmProviderException("groq returned HTTP 429"));
    when(mockHuggingFace.chat(anyList(), anyList())).thenReturn(HF_ANSWER);

    assertThat(client.chat(MESSAGES, List.of()).provider()).isEqualTo("huggingface");
  }

  @Test
  @DisplayName("after repeated failures the primary's circuit opens and it is skipped without being called")
  void openCircuitSkipsProvider() {
    when(mockGroq.chat(anyList(), anyList())).thenThrow(new LlmProviderException("timeout"));
    when(mockHuggingFace.chat(anyList(), anyList())).thenReturn(HF_ANSWER);

    client.chat(MESSAGES, List.of());
    client.chat(MESSAGES, List.of());
    assertThat(groqBreaker.state()).isEqualTo(CircuitBreaker.State.OPEN);
    client.chat(MESSAGES, List.of());

    verify(mockGroq, times(2)).chat(anyList(), anyList());
    verify(mockHuggingFace, times(3)).chat(anyList(), anyList());
  }

  @Test
  @DisplayName("all providers down, or none configured → AiUnavailableException (graceful 503)")
  void allDown() {
    when(mockGroq.chat(anyList(), anyList())).thenThrow(new LlmProviderException("timeout"));
    when(mockHuggingFace.chat(anyList(), anyList())).thenThrow(new LlmProviderException("502"));

    assertThatThrownBy(() -> client.chat(MESSAGES, List.of())).isInstanceOf(AiUnavailableException.class);
    assertThatThrownBy(() -> new FallbackLlmClient(List.of()).chat(MESSAGES, List.of()))
      .isInstanceOf(AiUnavailableException.class);
  }
}
