---
name: pr-review
description: Self-review checklist for the Bud-Wiser backend and frontend, run before marking any build phase done. Severity-tiered checks across security, user isolation, agent safety, API design, service/JPA layers, testing, query optimization, logging and reuse. Apply before committing or completing a phase.
---

# PR / Phase Review Checklist

Run this against the diff before marking any phase complete. Then hand the phase to the user to review and commit. Claude does not commit.

## Severity
- **CRITICAL (must fix):**
  - security holes: token or secret leak, auth bypass
  - **cross-user data access** (a query missing `user_id`)
  - **an agent write executed without confirmation**
  - the LLM able to supply the user id
  - a write without a transaction
  - data corruption
- **MAJOR (should fix):**
  - missing error handling, N+1 queries, missing input validation, wrong HTTP status
  - inline `@Query` or JPQL
  - missing `@Version`, duplicate logic
  - unbounded or `SELECT *` queries
  - money stored as `double`
  - bloated logging
  - an LLM doing arithmetic that the engine should do
- **MINOR:** style, naming, missing docs, dead code.
- **SUGGESTION:** reuse opportunities, other approaches, extra tests.

## Checklists

### Security / auth
- [ ] No secrets, keys or tokens committed (`.env`, PEMs, API keys).
- [ ] Identity comes from `ICurrentUserProvider` (JWT `sub`), not from request params.
- [ ] No tokens, passwords, PII or full LLM prompts in logs or error responses.
- [ ] Every user-data query is scoped by `user_id`, and other users' resources return 404.
- [ ] The refresh cookie is httpOnly, Secure and SameSite=Strict, stored hashed, and rotated.

### Agent safety (project-specific)
- [ ] The new tool is registered in `ToolRegistry` and its argument schema has **no userId**.
- [ ] Arguments are validated (Bean Validation plus business rules) before execution, and invalid ones return `REJECTED` with a reason.
- [ ] Write tools only create `PENDING_CONFIRMATION`. Confirmation is idempotent and owner-checked.
- [ ] Every tool call is audited in `agent_actions` (args, result, status, latency).
- [ ] Numbers in answers come from tool results. The system prompt tells the model not to compute them.
- [ ] Untrusted user text is passed as data, never inserted into instructions.
- [ ] LLM calls have a timeout, retry on 429 and 5xx only, and use a circuit breaker or fallback. The step limit is enforced.

### API design
- [ ] Paths are under `/api/v1/` and follow the interface + impl pattern.
- [ ] Endpoints return `Response<T>`, and controllers contain no business logic.
- [ ] Response DTO ids are `String`.
- [ ] HTTP methods and status codes are correct (201 create, 404 not found, 409 duplicate), and request DTOs are validated.
- [ ] List endpoints are paginated.

### Service layer
- [ ] `@Service` plus `@RequiredArgsConstructor`, constructor injection only.
- [ ] `@Transactional(rollbackFor = Exception.class)` on writes.
- [ ] Logs use the `[methodName]` prefix, and entity-to-DTO mapping goes through a MapStruct mapper.
- [ ] No catching generic `Exception`/`RuntimeException` to swallow it. Tool-result classification in the agent is the one allowed exception.

### JPA / DB
- [ ] All SQL is in `Queries.java` with `nativeQuery = true`. No JPQL, and `@Param` on every bind.
- [ ] `@Version` where updates can be concurrent, and `FetchType.LAZY`.
- [ ] `@Modifying(clearAutomatically = true)` for UPDATE and DELETE.
- [ ] Each Liquibase changeset has a unique id and the author `abhinash`. WHERE and JOIN columns are indexed, and NOT NULL columns have defaults.
- [ ] Money is `NUMERIC(14,2)` / `BigDecimal`.

### Query optimization
- [ ] No `SELECT *` on large tables, and WHERE clauses use indexed columns.
- [ ] No unbounded lists, and no functions on indexed columns in WHERE.

### Testing
- [ ] Unit tests cover new engine, rule, or service logic: the happy path, errors and edge cases.
- [ ] Agent changes are tested with `FakeLlmClient`.
- [ ] User-isolation integration tests are added for new endpoints.

### Frontend (when touched)
- [ ] No tokens in `localStorage`. The access token stays in memory, and refresh happens via the cookie.
- [ ] API calls go through `lib/api/client.ts`, and React Query keys stay consistent.
- [ ] Loading, empty and error states are handled, and the AI-unavailable state degrades gracefully.

### Code quality & logging
- [ ] No star imports, no commented-out or dead code, no magic numbers (use constants or config).
- [ ] Checked `common` and the engine for existing helpers before adding new ones.
- [ ] Only purposeful logs: none inside loops, no request-body dumps, and correct log levels.

## Output format
```
### Findings
#### Critical
- `[file:line]` issue — impact — fix
#### Major / Minor / Suggestions
- `[file:line]` ...
### Verdict: Approved | Approved with minor fixes | Request changes
```
