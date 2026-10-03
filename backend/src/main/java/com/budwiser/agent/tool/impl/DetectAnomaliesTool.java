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
public class DetectAnomaliesTool implements FinancialTool<MonthArgs> {
  private final IAnalyticsService analyticsService;

  @Override
  public String name() {
    return "detect_anomalies";
  }

  @Override
  public String description() {
    return "Unusual activity for a month: SPENDING_SPIKE (category > 30% above its 3-month average), "
      + "BUDGET_THRESHOLD (budget >= 80% used) and LARGE_TRANSACTION (> 3x the category median). Most severe first.";
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
    return analyticsService.anomalies(context.userId(), arguments.month());
  }
}
