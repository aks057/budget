---
name: jpa-database
description: JPA + PostgreSQL + Liquibase conventions for the Bud-Wiser backend — entity base class, @Version, FetchType.LAZY, all SQL as native-query constants in Queries.java (no inline @Query, no JPQL), every query scoped by user_id, Liquibase formatted-SQL changesets. Apply when adding entities, repositories, queries, or migrations.
---

# JPA & Database

## Entity template
```java
package com.budwiser.transaction.entity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = TableName.TRANSACTIONS)
public class Transaction extends AuditIdentifiableBase {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal amount;

  @Column(name = "type", length = 10, nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Version
  @Column(name = "version", nullable = false)
  private Integer version;
}
```
`AuditIdentifiableBase` (`common/entity`) carries `id` (BIGINT identity), `createdAt`, `modifiedAt` (epoch ms via `@PrePersist`/`@PreUpdate`).
Money is always `BigDecimal` / `NUMERIC(14,2)`. Never use `double`.

## Query management (CRITICAL)
1. **All SQL lives in `{feature}/constant/Queries.java`** (or `common/constant/Queries.java` for shared queries) as `public static final String` text blocks. Never inline SQL in `@Query`.
2. **Native SQL only:** always `nativeQuery = true`, never JPQL.
3. Native SQL uses **table and column names**, not entity or field names.
4. **Every query on user data has `user_id = :userId` in its WHERE clause.** This is the tenant boundary, the way `merchantId` is in the payment platform.

```java
public final class TransactionQueries {
  private TransactionQueries() {}

  public static final String SUM_BY_CATEGORY_IN_RANGE = """
    SELECT c.name AS category, t.type AS type, SUM(t.amount) AS total
    FROM transactions t
    JOIN categories c ON c.id = t.category_id
    WHERE t.user_id = :userId
      AND t.transaction_date >= :from AND t.transaction_date < :to
    GROUP BY c.name, t.type
    """;
}
```

```java
@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, Long> {

  Optional<Transaction> findByIdAndUserId(Long id, Long userId);   // derived query OK

  @Query(value = TransactionQueries.SUM_BY_CATEGORY_IN_RANGE, nativeQuery = true)
  List<CategoryTotalProjection> sumByCategory(@Param("userId") Long userId,
                                              @Param("from") LocalDate from,
                                              @Param("to") LocalDate to);
}
```
Never use `findById(id)` alone on user-owned data. Always use `findByIdAndUserId`.

## Liquibase migrations
Master file: `src/main/resources/db/changelog/db.changelog-master.yaml`, which uses `includeAll` on `db/changelog/changes/`.
Change files: `db/changelog/changes/YYYYMMDDHHmm-description.postgresql.sql`

```sql
-- liquibase formatted sql

-- changeset abhinash:create-transactions-1
CREATE TABLE transactions (
  id               BIGINT GENERATED ALWAYS AS IDENTITY,
  user_id          BIGINT        NOT NULL,
  amount           NUMERIC(14,2) NOT NULL,
  ...
  created_at       BIGINT        NOT NULL,
  modified_at      BIGINT        NOT NULL,
  version          INTEGER       DEFAULT 0 NOT NULL,
  CONSTRAINT transactions_pkey PRIMARY KEY (id),
  CONSTRAINT transactions_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT transactions_amount_chk CHECK (amount > 0)
);
-- rollback DROP TABLE transactions;

-- changeset abhinash:create-transactions-idx-2
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date DESC);
```

## Key rules
- Put queries in `Queries.java`, use native SQL only, and put `@Param` on every bind variable.
- Add `@Version` to any entity that can be updated concurrently.
- Use `FetchType.LAZY` for all relationships, and `@Modifying(clearAutomatically = true)` for UPDATE and DELETE.
- Keep table names in the `TableName` constants class.
- Changeset ids must be unique, descriptive, and carry the author `abhinash`.
- Add a `-- rollback` line where it makes sense.
- Index every column used in a WHERE or JOIN, and give NOT NULL columns a default value.
- `spring.jpa.hibernate.ddl-auto=validate`: Liquibase owns the schema, never Hibernate.
- `spring.jpa.open-in-view=false`.
