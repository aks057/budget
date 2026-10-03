---
name: testing-standards
description: Testing conventions for the Bud-Wiser backend — JUnit 5 + Mockito + AssertJ, Arrange/Act/Assert, @DisplayName, pure unit tests for the financial engine and anomaly rules, fake LlmClient for agent tests, Testcontainers Postgres for integration and user isolation, mocked security context. Apply when writing or reviewing tests.
---

# Testing Standards

## Frameworks
- **JUnit 5** (`@Test`, `@BeforeEach`, `@DisplayName`, `@Nested`, `@ParameterizedTest`).
- **Mockito** (`mock`, `when`, `verify`, `doThrow`) for Spring-layer collaborators.
- **AssertJ** (`assertThat(...)`), preferred over bare JUnit assertions.
- **Testcontainers** (`postgres:16`) for integration tests, via the shared `TestcontainersConfiguration` and `@ServiceConnection`.

## The test pyramid for this project
1. **Pure calculation unit tests.** No Spring, no mocks, no containers. Test exhaustively, because these are the core of the project:
   - `FinancialEngine` maths: savings rate, required monthly contribution, run-rate forecast, month-over-month and 3-month average
   - `AnomalyDetector` rules: thresholds and edge cases such as a zero 3-month average or no history
   - Use `BigDecimal` rounding cases.
2. **Spring-layer unit tests.** Mock the repositories, `ICurrentUserProvider`, and `LlmClient`.
3. **Agent tests** with a **`FakeLlmClient`** that returns scripted tool calls. Assert:
   - the tool sequence
   - that a write tool produces `PENDING_CONFIRMATION` and no DB write
   - that invalid args produce `REJECTED`
   - that the step limit is enforced
   - that a provider failure triggers the fallback or the graceful error
4. **Integration tests** (`@SpringBootTest` + Testcontainers + MockMvc):
   - auth flows (register, login, refresh rotation, reuse detection)
   - CRUD
   - **user isolation:** user B gets a 404 for user A's resources and sees nothing of user A's in list or analytics endpoints
5. **Evals** (the separate `./gradlew evalTest` task, run manually): run against the real LLM with seeded data. They are not part of `./gradlew build`.

## Example (pure unit)
```java
class GoalCalculatorTest {

  @Test
  @DisplayName("required monthly contribution spreads remaining amount over whole months left")
  void requiredMonthlyContribution() {
    // Arrange
    var goal = new GoalSnapshot(new BigDecimal("300000"), new BigDecimal("120000"), YearMonth.of(2027, 12));

    // Act
    var actual = GoalCalculator.progress(goal, YearMonth.of(2026, 12));

    // Assert
    assertThat(actual.remaining()).isEqualByComparingTo("180000");
    assertThat(actual.requiredMonthly()).isEqualByComparingTo("15000.00");
  }
}
```

## Mocking the current user
Prefer mocking `ICurrentUserProvider`. In MockMvc tests, use `.with(jwt().jwt(j -> j.subject("1")))` from `spring-security-test`.

## Conventions
- Name test classes `{ClassName}Test` and integration tests `{Feature}IT`. Use descriptive method names with `@DisplayName`.
- Name variables `expected*`, `actual*`, `mock*`.
- Always cover the happy path, the error cases (not found, validation), and the edge cases (empty month, single transaction, zero income).
- Verify key interactions, for example `verify(budgetRepository, never()).save(any())` for a pending agent action.
- Never call the real LLM in `./gradlew build`/`test`.
