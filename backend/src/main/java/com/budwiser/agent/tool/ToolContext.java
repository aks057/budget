package com.budwiser.agent.tool;

/**
 * Identity is injected by the server from the authenticated session — tool arguments never carry a user id.
 */
public record ToolContext(Long userId, Long conversationId) {}
