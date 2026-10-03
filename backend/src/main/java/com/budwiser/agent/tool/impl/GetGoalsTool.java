package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.support.ToolArgs;
import com.budwiser.agent.tool.support.ToolArgs.NoArgs;
import com.budwiser.goal.service.IGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GoalService scopes by the authenticated user of the current request — the same user as the ToolContext.
 */
@Component
@RequiredArgsConstructor
public class GetGoalsTool implements FinancialTool<NoArgs> {
  private final IGoalService goalService;

  @Override
  public String name() {
    return "get_goals";
  }

  @Override
  public String description() {
    return "The user's savings goals with target, saved so far, remaining, months left, required monthly "
      + "contribution and achieved/overdue flags.";
  }

  @Override
  public String parametersSchema() {
    return ToolArgs.NO_ARGS_SCHEMA;
  }

  @Override
  public Class<NoArgs> argumentsType() {
    return NoArgs.class;
  }

  @Override
  public Object execute(NoArgs arguments, ToolContext context) {
    return goalService.list();
  }
}
