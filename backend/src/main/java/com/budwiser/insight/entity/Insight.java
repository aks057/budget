package com.budwiser.insight.entity;

import com.budwiser.common.constant.TableName;
import com.budwiser.common.entity.AuditIdentifiableBase;
import com.budwiser.insight.constant.InsightSeverity;
import com.budwiser.insight.constant.InsightType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A proactive alert for one user. Rows are inserted with a native ON CONFLICT query (see InsightQueries);
 * the entity is used for reads and for marking as read.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TableName.INSIGHTS)
public class Insight extends AuditIdentifiableBase {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "type", length = 32, nullable = false)
  @Enumerated(EnumType.STRING)
  private InsightType type;

  @Column(name = "severity", length = 16, nullable = false)
  @Enumerated(EnumType.STRING)
  private InsightSeverity severity;

  @Column(name = "title", length = 120, nullable = false)
  private String title;

  @Column(name = "body", length = 500, nullable = false)
  private String body;

  @Column(name = "dedupe_key", length = 200, nullable = false)
  private String dedupeKey;

  /** Epoch millis; null while unread. */
  @Column(name = "read_at")
  private Long readAt;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
