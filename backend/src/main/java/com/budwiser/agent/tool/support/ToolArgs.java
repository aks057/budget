package com.budwiser.agent.tool.support;

import java.time.YearMonth;

/**
 * Shared argument shapes and schema fragments.
 */
public final class ToolArgs {
  private ToolArgs() {}

  public record NoArgs() {}

  /** month defaults to the current month when omitted. */
  public record MonthArgs(YearMonth month) {}

  public static final String NO_ARGS_SCHEMA = """
    {"type": "object", "properties": {}, "additionalProperties": false}
    """;

  public static final String MONTH_SCHEMA = """
    {
      "type": "object",
      "properties": {
        "month": {"type": "string", "pattern": "^\\\\d{4}-\\\\d{2}$",
                  "description": "Month as yyyy-MM. Omit for the current month."}
      },
      "additionalProperties": false
    }
    """;
}
