package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.impl.ListCategoriesTool.Args;
import com.budwiser.category.service.ICategoryService;
import com.budwiser.common.constant.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListCategoriesTool implements FinancialTool<Args> {
  private final ICategoryService categoryService;

  public record Args(TransactionType type) {}

  @Override
  public String name() {
    return "list_categories";
  }

  @Override
  public String description() {
    return "The user's income/expense category names. Use exact names from here in other tools.";
  }

  @Override
  public String parametersSchema() {
    return """
      {
        "type": "object",
        "properties": {"type": {"type": "string", "enum": ["EXPENSE", "INCOME"]}},
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
    return categoryService.list(arguments.type());
  }
}
