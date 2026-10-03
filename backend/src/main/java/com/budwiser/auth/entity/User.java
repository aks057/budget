package com.budwiser.auth.entity;

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
@Table(name = TableName.USERS)
public class User extends AuditIdentifiableBase {

  /** Always stored lower-cased. */
  @Column(name = "email", length = 320, nullable = false)
  private String email;

  /** Null for Google-only accounts. */
  @Column(name = "password_hash", length = 100)
  private String passwordHash;

  @Column(name = "full_name", length = 120, nullable = false)
  private String fullName;

  @Column(name = "avatar_url", length = 1024)
  private String avatarUrl;

  /** Google OIDC "sub" claim; null until the account signs in with Google. */
  @Column(name = "google_subject")
  private String googleSubject;

  @Column(name = "currency", length = 3, nullable = false)
  private String currency;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
