package com.budwiser.agent.tool.impl;

import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.impl.CreateGoalTool.Args;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.ValidationException;
import com.budwiser.common.util.MoneyFormat;
import com.budwiser.goal.dto.GoalRequest;
import com.budwiser.goal.service.IGoalService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateGoalTool implements FinancialTool<Args> {
  private final IGoalService goalService;
  private final Clock clock;

  public record Args(@NotBlank @Size(max = 100) String name,
                     @NotNull @DecimalMin("1") @Digits(integer = 12, fraction = 2) BigDecimal targetAmount,
                     @DecimalMin("0") @Digits(integer = 12, fraction = 2) BigDecimal currentAmount,
                     @NotNull LocalDate targetDate) {}

  @Override
  public String name() {
    return "create_goal";
  }

  @Override
  public String description() {
    return "Propose a new savings goal. Does not apply it: the user must confirm.";
  }

  @Override
  public String parametersSchema() {
    return """
      {
        "type": "object",
        "properties": {
          "name": {"type": "string"},
          "targetAmount": {"type": "number", "exclusiveMinimum": 0},
          "currentAmount": {"type": "number", "minimum": 0, "description": "Already saved, default 0"},
          "targetDate": {"type": "string", "format": "date", "description": "Deadline yyyy-MM-dd"}
        },
        "required": ["name", "targetAmount", "targetDate"],
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
    if (arguments.targetDate().isBefore(LocalDate.now(clock))) {
      throw new ValidationException(ErrorCode.GOAL_DATE_IN_PAST);
    }
    String saved = arguments.currentAmount() == null ? "" : " (" + MoneyFormat.inr(arguments.currentAmount()) + " already saved)";
    return "Create goal '" + arguments.name().trim() + "': " + MoneyFormat.inr(arguments.targetAmount())
      + " by " + arguments.targetDate() + saved;
  }

  @Override
  public Object execute(Args arguments, ToolContext context) {
    return goalService.create(new GoalRequest(arguments.name(), arguments.targetAmount(), arguments.currentAmount(),
      arguments.targetDate()));
  }
}
