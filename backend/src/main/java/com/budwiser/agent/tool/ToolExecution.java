package com.budwiser.agent.tool;

import com.budwiser.agent.llm.LlmToolCall;

public record ToolExecution(LlmToolCall call, ToolResult result, long latencyMs) {}
