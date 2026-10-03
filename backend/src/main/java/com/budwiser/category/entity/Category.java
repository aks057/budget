package com.budwiser.category.entity;

import com.budwiser.common.constant.TableName;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.entity.AuditIdentifiableBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TableName.CATEGORIES)
public class Category extends AuditIdentifiableBase {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "name", length = 50, nullable = false)
  private String name;

  /** Trimmed + lower-cased name, backing the case-insensitive unique index. */
  @Column(name = "name_key", length = 50, nullable = false)
  private String nameKey;

  @Column(name = "icon", length = 16, nullable = false)
  private String icon;

  @Column(name = "type", length = 10, nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionType type;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
