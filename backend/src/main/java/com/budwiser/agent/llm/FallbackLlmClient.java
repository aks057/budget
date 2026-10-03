package com.budwiser.agent.llm;

import com.budwiser.common.exception.AiUnavailableException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Tries providers in order (e.g. Groq, then Hugging Face), each behind its own circuit breaker. A provider failure
 * is classified, logged and skipped; only when every provider fails (or is OPEN) does the caller get
 * {@link AiUnavailableException} (HTTP 503 with a graceful message).
 */
@Slf4j
public class FallbackLlmClient implements LlmClient {

  public record ProviderSlot(OpenAiCompatibleLlmClient client, CircuitBreaker breaker) {}

  private final List<ProviderSlot> providers;

  public FallbackLlmClient(List<ProviderSlot> providers) {
    this.providers = List.copyOf(providers);
  }

  @Override
  public LlmResponse chat(List<LlmMessage> messages, List<LlmToolDefinition> tools) {
    if (providers.isEmpty()) {
      log.warn("[chat] no LLM provider configured (set GROQ_API_KEY and/or HF_TOKEN)");
      throw new AiUnavailableException();
    }
    for (ProviderSlot slot : providers) {
      String name = slot.client().providerName();
      if (!slot.breaker().tryAcquire()) {
        log.info("[chat] circuit open, skipping provider: {}", name);
        continue;
      }
      try {
        LlmResponse response = slot.client().chat(messages, tools);
        slot.breaker().recordSuccess();
        return response;
      } catch (LlmProviderException ex) {
        slot.breaker().recordFailure();
        log.warn("[chat] provider failed, trying next, provider: {}, reason: {}, circuit: {}",
          name, ex.getMessage(), slot.breaker().state());
      }
    }
    log.error("[chat] all LLM providers failed or unavailable, providers: {}", providers.size());
    throw new AiUnavailableException();
  }
}
