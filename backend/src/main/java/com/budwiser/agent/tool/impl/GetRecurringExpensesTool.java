package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.support.ToolArgs;
import com.budwiser.agent.tool.support.ToolArgs.NoArgs;
import com.budwiser.analytics.service.IAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetRecurringExpensesTool implements FinancialTool<NoArgs> {
  private final IAnalyticsService analyticsService;

  @Override
  public String name() {
    return "get_recurring_expenses";
  }

  @Override
  public String description() {
    return "Detected monthly recurring expenses (subscriptions, bills) from the last 6 months with typical amount "
      + "and next expected date.";
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
    return analyticsService.recurringExpenses(context.userId());
  }
}
