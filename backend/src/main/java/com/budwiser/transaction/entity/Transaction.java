package com.budwiser.transaction.entity;

import com.budwiser.common.constant.TableName;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.entity.AuditIdentifiableBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Category is referenced by id (not a JPA relation): list endpoints resolve names from the user's small category set
 * in one query, so there is no lazy-loading / N+1 risk.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TableName.TRANSACTIONS)
public class Transaction extends AuditIdentifiableBase {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  /** Copied from the category on every write. */
  @Column(name = "type", length = 10, nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionType type;

  @Column(name = "amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal amount;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "transaction_date", nullable = false)
  private LocalDate transactionDate;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
