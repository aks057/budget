package com.budwiser.agent.constant;

public final class AgentQueries {
  private AgentQueries() {}

  /** Most recent N messages of a conversation (newest first; the service reverses them). */
  public static final String RECENT_MESSAGES = """
    SELECT m.id, m.conversation_id, m.user_id, m.role, m.content, m.created_at, m.modified_at, m.version
    FROM agent_messages m
    WHERE m.conversation_id = :conversationId
      AND m.user_id = :userId
    ORDER BY m.id DESC
    LIMIT :limit
    """;
}
