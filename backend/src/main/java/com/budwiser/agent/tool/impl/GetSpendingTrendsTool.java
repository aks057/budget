package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.impl.GetSpendingTrendsTool.Args;
import com.budwiser.analytics.service.IAnalyticsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSpendingTrendsTool implements FinancialTool<Args> {
  private static final int DEFAULT_MONTHS = 6;

  private final IAnalyticsService analyticsService;

  public record Args(@Min(1) @Max(12) Integer months) {}

  @Override
  public String name() {
    return "get_spending_trends";
  }

  @Override
  public String description() {
    return "Monthly income, expenses, savings and savings rate for the last N months (oldest first, ending this month).";
  }

  @Override
  public String parametersSchema() {
    return """
      {
        "type": "object",
        "properties": {
          "months": {"type": "integer", "minimum": 1, "maximum": 12, "description": "Default 6"}
        },
        "additionalProperties": false
      }
      """;
  }

  @Override
  public Class<Args> argumentsType() {
    return Args.class;
  }

  @Override
  public Object execute(Args arguments, ToolContext context) {
    int months = arguments.months() != null ? arguments.months() : DEFAULT_MONTHS;
    return analyticsService.trends(context.userId(), months);
  }
}
