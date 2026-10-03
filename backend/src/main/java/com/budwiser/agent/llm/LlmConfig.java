package com.budwiser.agent.llm;

import java.net.http.HttpClient;
import java.time.Clock;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Configuration
public class LlmConfig {

  @Bean
  public LlmClient llmClient(LlmProperties properties, JsonMapper jsonMapper, Clock clock) {
    List<LlmProperties.Provider> configured = properties.providers() == null ? List.of()
      : properties.providers().stream().filter(LlmProperties.Provider::isConfigured).toList();
    log.info("[llmClient] LLM providers in fallback order: {}",
      configured.stream().map(LlmProperties.Provider::name).toList());
    LlmProperties.CircuitBreakerSettings breaker = properties.circuitBreaker();
    return new FallbackLlmClient(configured.stream()
      .map(provider -> new FallbackLlmClient.ProviderSlot(
        new OpenAiCompatibleLlmClient(provider, properties, restClient(provider, properties), jsonMapper),
        new CircuitBreaker(breaker.failureThreshold(), breaker.openDuration(), clock)))
      .toList());
  }

  /** Bounded connect and read timeouts: a hung provider must fail over, not hang the user's request. */
  private static RestClient restClient(LlmProperties.Provider provider, LlmProperties properties) {
    HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(properties.timeout());
    return RestClient.builder().baseUrl(provider.baseUrl()).requestFactory(requestFactory).build();
  }
}
