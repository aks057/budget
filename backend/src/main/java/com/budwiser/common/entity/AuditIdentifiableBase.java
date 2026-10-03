package com.budwiser.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

/**
 * Identity primary key plus epoch-millis audit timestamps shared by every table.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditIdentifiableBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Long createdAt;

  @Column(name = "modified_at", nullable = false)
  private Long modifiedAt;

  @PrePersist
  protected void onCreate() {
    long now = System.currentTimeMillis();
    createdAt = now;
    modifiedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    modifiedAt = System.currentTimeMillis();
  }
}
