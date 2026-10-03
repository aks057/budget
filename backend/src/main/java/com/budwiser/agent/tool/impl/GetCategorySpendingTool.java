package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.impl.GetCategorySpendingTool.Args;
import com.budwiser.analytics.service.IAnalyticsService;
import com.budwiser.common.constant.TransactionType;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategorySpendingTool implements FinancialTool<Args> {
  private final IAnalyticsService analyticsService;

  public record Args(LocalDate from, LocalDate to, TransactionType type) {}

  @Override
  public String name() {
    return "get_category_spending";
  }

  @Override
  public String description() {
    return "Totals per category (largest first, with % of total) for a date range. Defaults: current month, EXPENSE.";
  }

  @Override
  public String parametersSchema() {
    return """
      {
        "type": "object",
        "properties": {
          "from": {"type": "string", "format": "date", "description": "Start date yyyy-MM-dd (inclusive)"},
          "to": {"type": "string", "format": "date", "description": "End date yyyy-MM-dd (inclusive)"},
          "type": {"type": "string", "enum": ["EXPENSE", "INCOME"]}
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
    return analyticsService.categoryBreakdown(context.userId(), arguments.from(), arguments.to(), arguments.type());
  }
}
