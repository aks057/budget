package com.budwiser.transaction.dto;

import com.budwiser.common.constant.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDto {
  private String id;
  private BigDecimal amount;
  private TransactionType type;
  private String description;
  private LocalDate transactionDate;
  private String categoryId;
  private String categoryName;
  private String categoryIcon;
}
