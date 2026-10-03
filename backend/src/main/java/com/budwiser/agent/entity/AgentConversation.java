package com.budwiser.agent.entity;

import com.budwiser.common.constant.TableName;
import com.budwiser.common.entity.AuditIdentifiableBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TableName.AGENT_CONVERSATIONS)
public class AgentConversation extends AuditIdentifiableBase {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "title", length = 120, nullable = false)
  private String title;

  @Column(name = "last_message_at", nullable = false)
  private Long lastMessageAt;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
