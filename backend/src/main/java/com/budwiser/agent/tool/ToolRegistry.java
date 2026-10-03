package com.budwiser.agent.tool;

import com.budwiser.agent.llm.LlmToolDefinition;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Collects every {@link FinancialTool} bean. Fails fast at startup on duplicate names, invalid schemas,
 * or any schema that would let the model pass a user id.
 */
@Slf4j
@Component
public class ToolRegistry {
  private static final String FORBIDDEN_SCHEMA_TOKEN = "userid";

  private final Map<String, FinancialTool<?>> toolsByName = new LinkedHashMap<>();
  private final List<LlmToolDefinition> definitions;

  public ToolRegistry(List<FinancialTool<?>> tools, JsonMapper jsonMapper) {
    for (FinancialTool<?> tool : tools) {
      if (toolsByName.putIfAbsent(tool.name(), tool) != null) {
        throw new IllegalStateException("Duplicate agent tool name: " + tool.name());
      }
      if (tool.parametersSchema().toLowerCase(Locale.ROOT).replace("_", "").contains(FORBIDDEN_SCHEMA_TOKEN)) {
        throw new IllegalStateException("Tool " + tool.name() + " exposes a user id in its schema");
      }
    }
    this.definitions = toolsByName.values().stream()
      .map(tool -> new LlmToolDefinition(tool.name(), tool.description(), parseSchema(jsonMapper, tool)))
      .toList();
    log.info("[ToolRegistry] registered agent tools: {}", toolsByName.keySet());
  }

  public Optional<FinancialTool<?>> find(String name) {
    return Optional.ofNullable(name).map(toolsByName::get);
  }

  public List<LlmToolDefinition> definitions() {
    return definitions;
  }

  private static JsonNode parseSchema(JsonMapper jsonMapper, FinancialTool<?> tool) {
    JsonNode schema = jsonMapper.readTree(tool.parametersSchema());
    if (!schema.isObject() || !"object".equals(schema.path("type").stringValue())) {
      throw new IllegalStateException("Tool " + tool.name() + " schema must be a JSON object schema");
    }
    return schema;
  }
}
