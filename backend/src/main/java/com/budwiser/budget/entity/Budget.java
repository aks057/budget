package com.budwiser.budget.entity;

import com.budwiser.common.constant.TableName;
import com.budwiser.common.entity.AuditIdentifiableBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Standing monthly limit for one expense category. Spending vs. limit is computed per month on read.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TableName.BUDGETS)
public class Budget extends AuditIdentifiableBase {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  @Column(name = "amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal amount;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
