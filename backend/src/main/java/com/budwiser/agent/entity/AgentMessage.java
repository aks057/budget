package com.budwiser.agent.entity;

import com.budwiser.agent.constant.MessageRole;
import com.budwiser.common.constant.TableName;
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
@Table(name = TableName.AGENT_MESSAGES)
public class AgentMessage extends AuditIdentifiableBase {

  @Column(name = "conversation_id", nullable = false)
  private Long conversationId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "role", length = 16, nullable = false)
  @Enumerated(EnumType.STRING)
  private MessageRole role;

  @Column(name = "content", columnDefinition = "text", nullable = false)
  private String content;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
