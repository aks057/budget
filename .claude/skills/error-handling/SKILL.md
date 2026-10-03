---
name: error-handling
description: Exception and error-response conventions for the Bud-Wiser backend — exception hierarchy, ErrorCode/ExceptionType enums, global @RestControllerAdvice handler, Error builder, never swallow generic Exception, graceful AI-failure responses. Apply when adding error handling, throwing exceptions, or defining error responses.
---

# Error Handling

Adapted from the payment platform for Bud-Wiser (`com.budwiser.common.exception`).

## Exception Hierarchy
```
RuntimeException
└── BudWiserException                # base, carries List<Error>
    ├── ValidationException          # business-rule validation failures (400)
    ├── ResourceNotFoundException    # entity not found OR owned by another user (404)
    ├── ConflictException            # duplicate (e.g. budget already exists for category+month) (409)
    ├── AuthException                # bad credentials / invalid refresh token (401)
    ├── RateLimitExceededException   # per-user agent limit (429)
    └── AiUnavailableException       # LLM providers down / circuit open (503)
```
Plus Spring Security's `AccessDeniedException` (403) and `AuthenticationException` (401).

**Ownership rule:** fetching another user's resource throws `ResourceNotFoundException` (404), never 403, so ids of other users' data are not revealed.

## Throwing
```java
var budget = budgetRepository.findByIdAndUserId(id, userId)
  .orElseThrow(() -> new ResourceNotFoundException(id, ErrorCode.BUDGET_NOT_FOUND));

List<Error> errors = new ArrayList<>();
if (request.getTargetDate().isBefore(LocalDate.now())) {
  errors.add(Error.of(ErrorCode.GOAL_DATE_IN_PAST));
}
if (!errors.isEmpty()) {
  throw new ValidationException("Validation failed", errors);
}
```

## Error structure
```java
@Getter @Setter @Builder
public class Error {
  private String id;        // related entity id
  private String type;      // ExceptionType name
  private String code;      // machine-readable code, e.g. "BW-2001"
  private String message;   // human-readable
  private Object errorInfo; // optional extra detail (e.g. field name)
}
```

## Global Handler
Location: `common/exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`)

| Exception | HTTP |
|---|---|
| `ValidationException`, `MethodArgumentNotValidException`, `HttpMessageNotReadableException` | 400 |
| `AuthException`, `AuthenticationException` | 401 |
| `AccessDeniedException` | 403 |
| `ResourceNotFoundException` | 404 |
| `ConflictException` | 409 |
| `RateLimitExceededException` | 429 |
| `AiUnavailableException` | 503 (friendly message: dashboard still works) |
| anything else | 500 generic message, logged with requestId |

Each handler returns `Response<Void>` with the `errors` list. Never leak stack traces, SQL, or provider error bodies to clients.

## ErrorCode enum (`common/constant/ErrorCode.java`)
Codes are grouped by feature: `BW-1xxx` auth, `BW-2xxx` transaction, `BW-3xxx` budget, `BW-4xxx` goal, `BW-5xxx` agent, `BW-9xxx` generic.

## Agent-specific rule
Tool failures inside the agent loop do **not** throw to the client. `ToolExecutor` turns validation and business exceptions into a structured `ToolResult.rejected(reason)` or `ToolResult.error(code)` and feeds that back to the LLM. This is control flow (classification), not swallowing, and every case is logged plus written to `agent_actions`. Only `AiUnavailableException` (all providers down) reaches the client, as an SSE `error` event.

## Best practices
1. Log at the boundary: `log.error("[methodName] failure, id: {}", id, ex)`.
2. Use specific exception types, never generic `Exception`/`RuntimeException`.
3. Never catch `Exception`/`RuntimeException` just to swallow it; let it roll back and bubble up to the handler.
4. Include entity ids in error responses.
5. Group related validation failures into one exception with several `Error`s.
