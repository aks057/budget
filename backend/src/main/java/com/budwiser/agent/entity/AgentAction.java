package com.budwiser.agent.entity;

import com.budwiser.agent.constant.ActionStatus;
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

/**
 * One audited tool call (PRD §22). For write tools it is also the pending action the user must confirm;
 * @Version makes "confirm" safe against double-clicks and concurrent requests.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TableName.AGENT_ACTIONS)
public class AgentAction extends AuditIdentifiableBase {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "conversation_id")
  private Long conversationId;

  @Column(name = "tool_name", length = 64, nullable = false)
  private String toolName;

  /** Raw arguments exactly as the model produced them. */
  @Column(name = "arguments", columnDefinition = "text", nullable = false)
  private String arguments;

  @Column(name = "result", columnDefinition = "text")
  private String result;

  /** Human-readable description of a pending write, shown on the confirm card. */
  @Column(name = "summary", length = 500)
  private String summary;

  @Column(name = "status", length = 24, nullable = false)
  @Enumerated(EnumType.STRING)
  private ActionStatus status;

  @Column(name = "latency_ms", nullable = false)
  private Integer latencyMs;

  @Column(name = "expires_at")
  private Long expiresAt;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
