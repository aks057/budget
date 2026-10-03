package com.budwiser.agent.llm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * @param providers tried in order; a provider without an API key is skipped (not configured)
 */
@Validated
@ConfigurationProperties(prefix = "budwiser.llm")
public record LlmProperties(@NotNull Duration timeout, int maxTokens, double temperature,
                            @Valid @NotNull CircuitBreakerSettings circuitBreaker, List<Provider> providers) {

  public record Provider(String name, String baseUrl, String model, String apiKey) {

    public boolean isConfigured() {
      return StringUtils.hasText(apiKey) && StringUtils.hasText(baseUrl) && StringUtils.hasText(model);
    }
  }

  public record CircuitBreakerSettings(@Min(1) int failureThreshold, @NotNull Duration openDuration) {}
}
