package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.impl.UpdateBudgetTool.Args;
import com.budwiser.agent.tool.support.CategoryResolver;
import com.budwiser.budget.dto.BudgetDto;
import com.budwiser.budget.dto.UpdateBudgetRequest;
import com.budwiser.budget.service.IBudgetService;
import com.budwiser.category.entity.Category;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.exception.ResourceNotFoundException;
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
public class UpdateBudgetTool implements FinancialTool<Args> {
  private final IBudgetService budgetService;
  private final CategoryResolver categoryResolver;

  public record Args(@NotBlank @Size(max = 50) String categoryName,
                     @NotNull @DecimalMin("1") @Digits(integer = 12, fraction = 2) BigDecimal amount) {}

  @Override
  public String name() {
    return "update_budget";
  }

  @Override
  public String description() {
    return "Propose changing the monthly limit of an existing category budget. Does not apply it: the user must confirm.";
  }

  @Override
  public String parametersSchema() {
    return """
      {
        "type": "object",
        "properties": {
          "categoryName": {"type": "string", "description": "Exact expense category name"},
          "amount": {"type": "number", "exclusiveMinimum": 0, "description": "New monthly limit in INR"}
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
    BudgetDto current = existingBudget(context, arguments.categoryName());
    return "Change the " + current.getCategoryName() + " budget from " + MoneyFormat.inr(current.getAmount())
      + " to " + MoneyFormat.inr(arguments.amount()) + " per month";
  }

  @Override
  public Object execute(Args arguments, ToolContext context) {
    BudgetDto current = existingBudget(context, arguments.categoryName());
    return budgetService.update(Long.valueOf(current.getId()), new UpdateBudgetRequest(arguments.amount()));
  }

  private BudgetDto existingBudget(ToolContext context, String categoryName) {
    Category category = categoryResolver.resolve(context.userId(), categoryName, TransactionType.EXPENSE);
    return budgetService.getStatuses(context.userId(), null).stream()
      .filter(budget -> budget.getCategoryId().equals(String.valueOf(category.getId())))
      .findFirst()
      .orElseThrow(() -> new ResourceNotFoundException(category.getName(), ErrorCode.BUDGET_NOT_FOUND));
  }
}
