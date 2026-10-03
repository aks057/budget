package com.budwiser.agent.orchestrator;

import com.budwiser.agent.tool.ToolExecution;
import java.util.List;

/**
 * Outcome of one user message: the final reply plus everything the agent did to get there.
 */
public record AgentTurn(String reply, List<ToolExecution> executions, int steps, String provider) {}
