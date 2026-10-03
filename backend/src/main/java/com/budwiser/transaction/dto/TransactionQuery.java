package com.budwiser.transaction.dto;

import com.budwiser.common.constant.TransactionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Query-string filters for GET /transactions. from/to default to the current month.
 */
@Getter
@Setter
@NoArgsConstructor
public class TransactionQuery {
  public static final int DEFAULT_PAGE_SIZE = 20;
  public static final int MAX_PAGE_SIZE = 100;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate from;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate to;

  private TransactionType type;

  private Long categoryId;

  @Min(value = 0, message = "Page must be 0 or greater")
  private int page = 0;

  @Min(value = 1, message = "Size must be at least 1")
  @Max(value = MAX_PAGE_SIZE, message = "Size must be at most 100")
  private int size = DEFAULT_PAGE_SIZE;
}
