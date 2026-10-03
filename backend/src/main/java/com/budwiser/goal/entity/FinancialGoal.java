package com.budwiser.goal.entity;

import com.budwiser.common.constant.TableName;
import com.budwiser.common.entity.AuditIdentifiableBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TableName.FINANCIAL_GOALS)
public class FinancialGoal extends AuditIdentifiableBase {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "name", length = 100, nullable = false)
  private String name;

  @Column(name = "target_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal targetAmount;

  @Column(name = "current_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal currentAmount;

  @Column(name = "target_date", nullable = false)
  private LocalDate targetDate;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
