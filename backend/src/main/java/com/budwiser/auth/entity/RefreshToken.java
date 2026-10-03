package com.budwiser.auth.entity;

import com.budwiser.common.constant.TableName;
import com.budwiser.common.entity.AuditIdentifiableBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TableName.REFRESH_TOKENS)
public class RefreshToken extends AuditIdentifiableBase {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  /** Hex SHA-256 of the raw token; the raw value only ever lives in the client's cookie. */
  @Column(name = "token_hash", length = 64, nullable = false)
  private String tokenHash;

  @Column(name = "family_id", nullable = false)
  private UUID familyId;

  @Column(name = "expires_at", nullable = false)
  private Long expiresAt;

  @Column(name = "revoked_at")
  private Long revokedAt;

  @Column(name = "user_agent")
  private String userAgent;

  /** Two concurrent rotations of the same token: one wins, the other fails with an optimistic-lock conflict. */
  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
