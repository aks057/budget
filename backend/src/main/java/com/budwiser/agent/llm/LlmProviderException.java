package com.budwiser.agent.llm;

/**
 * A single provider failed (timeout, 429, 5xx, unparseable response). Internal to the LLM layer: the fallback
 * client catches it and tries the next provider; only when all fail does the user see AiUnavailableException.
 */
public class LlmProviderException extends RuntimeException {

  public LlmProviderException(String message) {
    super(message);
  }

  public LlmProviderException(String message, Throwable cause) {
    super(message, cause);
  }
}
