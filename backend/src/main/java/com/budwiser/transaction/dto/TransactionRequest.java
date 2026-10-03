package com.budwiser.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body for create (POST) and full update (PUT). Type is not accepted — it is derived from the category.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  @Digits(integer = 12, fraction = 2, message = "Amount must have at most 12 digits and 2 decimals")
  private BigDecimal amount;

  @NotNull(message = "Category is required")
  private Long categoryId;

  @Size(max = 255, message = "Description must be at most 255 characters")
  private String description;

  @NotNull(message = "Transaction date is required")
  private LocalDate transactionDate;
}
