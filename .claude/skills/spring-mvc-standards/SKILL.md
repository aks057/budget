---
name: spring-mvc-standards
description: Spring MVC controller/service conventions for the Bud-Wiser backend — interface+impl split, Response<T> wrapper, /api/v1/ versioning, constructor injection, [methodName] logging, IDs-as-String in responses, userId from the security context, no business logic in controllers. Apply when writing any controller, service, or REST endpoint.
---

# Spring MVC Standards

Adapted from the payment platform for Bud-Wiser (base package `com.budwiser`). Code is organised feature-first (`auth`, `transaction`, `budget`, `goal`, `analytics`, `agent`, `insight`, ...). Each feature has `controller`, `controller/impl`, `service`, `service/impl`, `repository`, `entity`, `dto` and `mapper`.

## Controller Interface Pattern
Location: `{feature}/controller/IXxxController.java`

```java
package com.budwiser.transaction.controller;

@RestController
@RequestMapping("/api/v1/transactions")
public interface ITransactionController {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  Response<TransactionDto> create(@Valid @RequestBody CreateTransactionRequest request);

  @GetMapping("/{id}")
  Response<TransactionDto> getById(@PathVariable Long id);
}
```

## Controller Implementation Pattern
Location: `{feature}/controller/impl/XxxController.java`

```java
package com.budwiser.transaction.controller.impl;

@Component  // NOT @RestController — the interface carries it
@RequiredArgsConstructor
public class TransactionController implements ITransactionController {
  private final ITransactionService transactionService;

  @Override
  public Response<TransactionDto> create(CreateTransactionRequest request) {
    return Response.<TransactionDto>builder().data(transactionService.create(request)).build();
  }
}
```

## Service Interface + Implementation
Location: `{feature}/service/IXxxService.java`, `{feature}/service/impl/XxxService.java`

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
  private final ITransactionRepository transactionRepository;
  private final ITransactionMapper transactionMapper;
  private final ICurrentUserProvider currentUser;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public TransactionDto create(CreateTransactionRequest request) {
    Long userId = currentUser.getUserId();
    log.info("[create] creating transaction, userId: {}, type: {}", userId, request.getType());
    ...
  }
}
```

## Key Conventions
1. **Interface-based:** always `IXxxController`/`IXxxService` + impl.
2. **Annotations:** `@RestController` on the interface, `@Component` on the controller impl, `@Service` on the service impl.
3. **Injection:** constructor injection via Lombok `@RequiredArgsConstructor` — never field `@Autowired`.
4. **Transactions:** `@Transactional(rollbackFor = Exception.class)` on every write; `@Transactional(readOnly = true)` on read services.
5. **Logging:** `log.info("[methodName] description, param: {}", param)` — concise; never log amounts+descriptions together as a dump, tokens, or LLM prompts in full.
6. **Response:** always return `Response.<T>builder().data(result).build()`.
7. **HTTP methods:** GET (read), POST (create), PUT (full update), PATCH (partial), DELETE. 201 for creates.
8. **Versioning:** every path starts with `/api/v1/` (Caddy routes `/api/*` to Spring). Internal job endpoints use `/internal/`.
9. **Controllers carry no business logic** — only delegate to a service and wrap the response.
10. **IDs as String in responses:** response DTO `id`/`*Id` fields are `String` (MapStruct converts `Long → String`). Request DTOs and `@PathVariable Long id` stay `Long`.
11. **User scoping:** the user id ALWAYS comes from `ICurrentUserProvider` (backed by the validated JWT), never from a request body, query param, or LLM tool argument.

## Anti-pattern (DO NOT)
```java
// ❌ business logic in controller
public Response<BudgetStatusDto> status(Long id) {
  var spent = transactionRepository.sumByCategory(...); // ❌ belongs in service/engine
  ...
}
// ❌ userId from the client
public Response<List<TransactionDto>> list(@RequestParam Long userId) { ... }
```

## Response Wrapper
All endpoints return `Response<T>` (`timestamp`, `status`, `message`, `data`, `errors`, optional `page` metadata). Defined once in `com.budwiser.common.response`. Exception: the SSE agent chat endpoint streams `SseEmitter` events.
