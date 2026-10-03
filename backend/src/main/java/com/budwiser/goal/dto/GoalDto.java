package com.budwiser.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoalDto {
  private String id;
  private String name;
  private BigDecimal targetAmount;
  private BigDecimal currentAmount;
  private LocalDate targetDate;
  private BigDecimal remaining;
  private BigDecimal percentComplete;
  private long monthsRemaining;
  private BigDecimal requiredMonthlyContribution;
  private boolean achieved;
  private boolean overdue;
}
