package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.support.ToolArgs;
import com.budwiser.agent.tool.support.ToolArgs.MonthArgs;
import com.budwiser.budget.service.IBudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBudgetsTool implements FinancialTool<MonthArgs> {
  private final IBudgetService budgetService;

  @Override
  public String name() {
    return "get_budgets";
  }

  @Override
  public String description() {
    return "The user's monthly category budgets with amount spent, remaining (negative = overspent), % used and "
      + "level ON_TRACK / WARNING (>=80%) / EXCEEDED (>=100%) for a month.";
  }

  @Override
  public String parametersSchema() {
    return ToolArgs.MONTH_SCHEMA;
  }

  @Override
  public Class<MonthArgs> argumentsType() {
    return MonthArgs.class;
  }

  @Override
  public Object execute(MonthArgs arguments, ToolContext context) {
    return budgetService.getStatuses(context.userId(), arguments.month());
  }
}
