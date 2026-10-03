package com.budwiser.agent.llm;

import tools.jackson.databind.JsonNode;

/**
 * @param parameters JSON Schema of the tool arguments (never contains a user id)
 */
public record LlmToolDefinition(String name, String description, JsonNode parameters) {}
