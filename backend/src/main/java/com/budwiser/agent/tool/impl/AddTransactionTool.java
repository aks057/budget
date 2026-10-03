package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.impl.AddTransactionTool.Args;
import com.budwiser.agent.tool.support.CategoryResolver;
import com.budwiser.category.entity.Category;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.exception.ValidationException;
import com.budwiser.common.util.MoneyFormat;
import com.budwiser.transaction.dto.TransactionRequest;
import com.budwiser.transaction.service.ITransactionService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Natural-language entry: "spent 450 on Swiggy yesterday" → a pending transaction the user confirms.
 */
@Component
@RequiredArgsConstructor
public class AddTransactionTool implements FinancialTool<Args> {
  private final ITransactionService transactionService;
  private final CategoryResolver categoryResolver;
  private final Clock clock;

  public record Args(@NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
                     @NotBlank @Size(max = 50) String categoryName,
                     TransactionType type,
                     @Size(max = 255) String description,
                     LocalDate date) {}

  @Override
  public String name() {
    return "add_transaction";
  }

  @Override
  public String description() {
    return "Propose recording an income or expense. Type comes from the category. Does not apply it: the user "
      + "must confirm. date defaults to today.";
  }

  @Override
  public String parametersSchema() {
    return """
      {
        "type": "object",
        "properties": {
          "amount": {"type": "number", "exclusiveMinimum": 0},
          "categoryName": {"type": "string", "description": "Exact category name"},
          "type": {"type": "string", "enum": ["EXPENSE", "INCOME"],
                   "description": "Only needed if the name exists for both types"},
          "description": {"type": "string", "description": "e.g. merchant name"},
          "date": {"type": "string", "format": "date", "description": "yyyy-MM-dd, default today"}
        },
        "required": ["amount", "categoryName"],
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
    Category category = categoryResolver.resolve(context.userId(), arguments.categoryName(), arguments.type());
    LocalDate date = dateOrToday(arguments);
    if (date.isAfter(LocalDate.now(clock))) {
      throw new ValidationException(ErrorCode.TRANSACTION_DATE_IN_FUTURE);
    }
    String note = StringUtils.hasText(arguments.description()) ? " (" + arguments.description().trim() + ")" : "";
    return "Add " + category.getType().name().toLowerCase(Locale.ROOT) + " of " + MoneyFormat.inr(arguments.amount())
      + " in " + category.getName() + " on " + date + note;
  }

  @Override
  public Object execute(Args arguments, ToolContext context) {
    Category category = categoryResolver.resolve(context.userId(), arguments.categoryName(), arguments.type());
    return transactionService.create(new TransactionRequest(arguments.amount(), category.getId(),
      arguments.description(), dateOrToday(arguments)));
  }

  private LocalDate dateOrToday(Args arguments) {
    return arguments.date() != null ? arguments.date() : LocalDate.now(clock);
  }
}
