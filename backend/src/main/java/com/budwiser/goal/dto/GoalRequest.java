package com.budwiser.goal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body for create (POST) and full update (PUT). currentAmount defaults to 0.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must be at most 100 characters")
  private String name;

  @NotNull(message = "Target amount is required")
  @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
  @Digits(integer = 12, fraction = 2, message = "Target amount must have at most 12 digits and 2 decimals")
  private BigDecimal targetAmount;

  @DecimalMin(value = "0.00", message = "Current amount cannot be negative")
  @Digits(integer = 12, fraction = 2, message = "Current amount must have at most 12 digits and 2 decimals")
  private BigDecimal currentAmount;

  @NotNull(message = "Target date is required")
  private LocalDate targetDate;
}
