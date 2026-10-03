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
public class GetMonthlySummaryTool implements FinancialTool<MonthArgs> {
  private final IAnalyticsService analyticsService;

  @Override
  public String name() {
    return "get_monthly_summary";
  }

  @Override
  public String description() {
    return "Income, expenses, savings and savings rate for a month, the previous month's expenses with % change, "
      + "and (for the current month) a run-rate forecast of month-end spending.";
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
    return analyticsService.summary(context.userId(), arguments.month());
  }
}
