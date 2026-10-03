package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.impl.CreateBudgetTool.Args;
import com.budwiser.agent.tool.support.CategoryResolver;
import com.budwiser.budget.dto.CreateBudgetRequest;
import com.budwiser.budget.service.IBudgetService;
import com.budwiser.category.entity.Category;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.exception.ConflictException;
import com.budwiser.common.util.MoneyFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateBudgetTool implements FinancialTool<Args> {
  private final IBudgetService budgetService;
  private final CategoryResolver categoryResolver;

  public record Args(@NotBlank @Size(max = 50) String categoryName,
                     @NotNull @DecimalMin("1") @Digits(integer = 12, fraction = 2) BigDecimal amount) {}

  @Override
  public String name() {
    return "create_budget";
  }

  @Override
  public String description() {
    return "Propose a new standing monthly budget for an EXPENSE category. Does not apply it: the user must "
      + "confirm. Use update_budget if the category already has a budget.";
  }

  @Override
  public String parametersSchema() {
    return """
      {
        "type": "object",
        "properties": {
          "categoryName": {"type": "string", "description": "Exact expense category name"},
          "amount": {"type": "number", "exclusiveMinimum": 0, "description": "Monthly limit in INR"}
        },
        "required": ["categoryName", "amount"],
        "additionalProperties": false
      }
      """;
  }

  @Override
  public Class<Args> argumentsType() {
    return Args.class;
  }

  @Override
  public boolean requiresConfirmation() {
    return true;
  }

  @Override
  public String describe(Args arguments, ToolContext context) {
    Category category = categoryResolver.resolve(context.userId(), arguments.categoryName(), TransactionType.EXPENSE);
    boolean exists = budgetService.getStatuses(context.userId(), null).stream()
      .anyMatch(budget -> budget.getCategoryId().equals(String.valueOf(category.getId())));
    if (exists) {
      throw new ConflictException(ErrorCode.BUDGET_ALREADY_EXISTS);
    }
    return "Create a monthly budget of " + MoneyFormat.inr(arguments.amount()) + " for " + category.getName();
  }

  @Override
  public Object execute(Args arguments, ToolContext context) {
    Category category = categoryResolver.resolve(context.userId(), arguments.categoryName(), TransactionType.EXPENSE);
    return budgetService.create(new CreateBudgetRequest(category.getId(), arguments.amount()));
  }
}
