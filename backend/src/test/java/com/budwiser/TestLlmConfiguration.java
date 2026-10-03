package com.budwiser;

import com.budwiser.support.FakeLlmClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Integration tests never call a real LLM: the scripted fake replaces the provider chain.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestLlmConfiguration {

  @Bean
  @Primary
  public FakeLlmClient fakeLlmClient() {
    return new FakeLlmClient();
  }
}
