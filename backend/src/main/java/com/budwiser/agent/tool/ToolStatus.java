package com.budwiser.agent.tool;

public enum ToolStatus {
  /** Read tool ran; data is in the result. */
  SUCCESS,
  /** Arguments or business rules rejected the call; the reason goes back to the model so it can self-correct. */
  REJECTED,
  /** Unexpected failure inside the tool. */
  ERROR,
  /** Write tool validated and queued; nothing changes until the user confirms. */
  PENDING_CONFIRMATION
}
