package com.budwiser.agent.constant;

/**
 * Lifecycle of an audited tool call.
 * Read tools end in SUCCESS / REJECTED / ERROR immediately.
 * Write tools: PENDING_CONFIRMATION → EXECUTING → EXECUTED | FAILED, or → CANCELLED / EXPIRED.
 */
public enum ActionStatus {
  SUCCESS,
  REJECTED,
  ERROR,
  PENDING_CONFIRMATION,
  EXECUTING,
  EXECUTED,
  FAILED,
  CANCELLED,
  EXPIRED
}
