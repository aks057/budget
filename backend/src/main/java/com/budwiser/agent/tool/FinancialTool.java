package com.budwiser.agent.tool;

/**
 * A capability the agent may use — the ONLY way the LLM can touch user data (PRD §4, §14). Implementations are
 * thin adapters over the domain services, so every rule and every user_id scope of the REST API applies here too.
 *
 * @param <A> arguments record, deserialized from the model's JSON and Bean-Validated before any execution
 */
public interface FinancialTool<A> {

  /** snake_case name exposed to the model. */
  String name();

  /** Tells the model when to use the tool. */
  String description();

  /** JSON Schema of {@code A}. Must not contain a user id (enforced at startup by the registry). */
  String parametersSchema();

  Class<A> argumentsType();

  /** Write tools return true: the executor queues them for user confirmation instead of executing. */
  default boolean requiresConfirmation() {
    return false;
  }

  /** Read tools: run now. Write tools: run only after the user confirms the pending action. */
  Object execute(A arguments, ToolContext context);

  /**
   * Write tools only: check business rules and describe the change in one line, without writing anything.
   * Throwing a BudWiserException here rejects the call with that reason.
   */
  default String describe(A arguments, ToolContext context) {
    throw new UnsupportedOperationException(name() + " is not a write tool");
  }
}
