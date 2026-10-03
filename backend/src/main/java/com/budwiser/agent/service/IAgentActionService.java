package com.budwiser.agent.service;

import com.budwiser.agent.dto.AgentDtos.ActionResultDto;

public interface IAgentActionService {

  /** Executes a pending write the agent proposed. Idempotent: a second confirm is a 409. */
  ActionResultDto confirm(Long actionId);

  ActionResultDto reject(Long actionId);
}
