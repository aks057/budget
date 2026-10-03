package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.support.ToolArgs;
import com.budwiser.agent.tool.support.ToolArgs.MonthArgs;
import com.budwiser.analytics.service.IAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompareSpendingTool implements FinancialTool<MonthArgs> {
  private final IAnalyticsService analyticsService;

  @Override
  public String name() {
    return "compare_spending";
  }

  @Override
  public String description() {
    return "Per expense category: this month vs previous month vs the average of the 3 months before, with % changes. "
      + "Use it to explain WHY spending changed. For the running month 'current' is month-to-date.";
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
    return analyticsService.comparison(context.userId(), arguments.month());
  }
}
