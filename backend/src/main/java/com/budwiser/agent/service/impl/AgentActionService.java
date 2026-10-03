package com.budwiser.agent.service.impl;

import com.budwiser.agent.constant.ActionStatus;
import com.budwiser.agent.constant.AgentConstants;
import com.budwiser.agent.dto.AgentDtos.ActionResultDto;
import com.budwiser.agent.entity.AgentAction;
import com.budwiser.agent.repository.IAgentActionRepository;
import com.budwiser.agent.service.IAgentActionService;
import com.budwiser.agent.tool.FinancialTool;
import com.budwiser.agent.tool.ToolContext;
import com.budwiser.agent.tool.ToolExecutor;
import com.budwiser.agent.tool.ToolRegistry;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.BudWiserException;
import com.budwiser.common.exception.ConflictException;
import com.budwiser.common.exception.ResourceNotFoundException;
import com.budwiser.security.service.ICurrentUserProvider;
import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Human-in-the-loop execution of agent writes, in three short transactions:
 * 1. claim: PENDING_CONFIRMATION → EXECUTING (@Version makes double-confirm lose with 409)
 * 2. execute the domain write in its own transaction (the service's)
 * 3. record EXECUTED or FAILED
 * Splitting them means a business failure in step 2 is recorded instead of rolled back with everything else.
 */
@Slf4j
@Service
public class AgentActionService implements IAgentActionService {
  private final IAgentActionRepository actionRepository;
  private final ToolRegistry toolRegistry;
  private final ToolExecutor toolExecutor;
  private final ICurrentUserProvider currentUserProvider;
  private final TransactionTemplate transactionTemplate;
  private final JsonMapper jsonMapper;
  private final Clock clock;

  public AgentActionService(IAgentActionRepository actionRepository, ToolRegistry toolRegistry,
                            ToolExecutor toolExecutor, ICurrentUserProvider currentUserProvider,
                            PlatformTransactionManager transactionManager, JsonMapper jsonMapper, Clock clock) {
    this.actionRepository = actionRepository;
    this.toolRegistry = toolRegistry;
    this.toolExecutor = toolExecutor;
    this.currentUserProvider = currentUserProvider;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.jsonMapper = jsonMapper;
    this.clock = clock;
  }

  @Override
  public ActionResultDto confirm(Long actionId) {
    Long userId = currentUserProvider.getUserId();
    AgentAction claimed = transactionTemplate.execute(status -> claim(userId, actionId));
    if (claimed == null) {
      throw new ConflictException(ErrorCode.ACTION_EXPIRED);
    }

    FinancialTool<?> tool = toolRegistry.find(claimed.getToolName())
      .orElseThrow(() -> new IllegalStateException("Pending action references unknown tool " + claimed.getToolName()));
    try {
      Object data = runConfirmed(tool, claimed);
      complete(actionId, ActionStatus.EXECUTED, jsonMapper.writeValueAsString(data));
      log.info("[confirm] agent action executed, userId: {}, actionId: {}, tool: {}", userId, actionId, tool.name());
      return new ActionResultDto(String.valueOf(actionId), ActionStatus.EXECUTED, claimed.getSummary(), data);
    } catch (BudWiserException ex) {
      // e.g. a budget for that category was created manually since the proposal: record why, tell the user.
      complete(actionId, ActionStatus.FAILED, ex.getMessage());
      log.info("[confirm] agent action failed business rules, userId: {}, actionId: {}", userId, actionId);
      return new ActionResultDto(String.valueOf(actionId), ActionStatus.FAILED, ex.getMessage(), null);
    }
  }

  @Override
  public ActionResultDto reject(Long actionId) {
    Long userId = currentUserProvider.getUserId();
    AgentAction action = transactionTemplate.execute(status -> {
      AgentAction pending = getPending(userId, actionId);
      pending.setStatus(ActionStatus.CANCELLED);
      return pending;
    });
    log.info("[reject] agent action cancelled, userId: {}, actionId: {}", userId, actionId);
    return new ActionResultDto(String.valueOf(actionId), ActionStatus.CANCELLED, action.getSummary(), null);
  }

  /** Returns null when the action has expired (the EXPIRED status is committed, then the caller throws). */
  private AgentAction claim(Long userId, Long actionId) {
    AgentAction action = getPending(userId, actionId);
    if (action.getExpiresAt() != null && action.getExpiresAt() < clock.millis()) {
      action.setStatus(ActionStatus.EXPIRED);
      return null;
    }
    action.setStatus(ActionStatus.EXECUTING);
    actionRepository.saveAndFlush(action);
    return action;
  }

  private AgentAction getPending(Long userId, Long actionId) {
    AgentAction action = actionRepository.findByIdAndUserId(actionId, userId)
      .orElseThrow(() -> new ResourceNotFoundException(actionId, ErrorCode.ACTION_NOT_FOUND));
    if (action.getStatus() != ActionStatus.PENDING_CONFIRMATION) {
      throw new ConflictException(ErrorCode.ACTION_ALREADY_PROCESSED);
    }
    return action;
  }

  private <A> Object runConfirmed(FinancialTool<A> tool, AgentAction action) {
    A arguments = toolExecutor.parseValidated(tool, action.getArguments());
    return tool.execute(arguments, new ToolContext(action.getUserId(), action.getConversationId()));
  }

  private void complete(Long actionId, ActionStatus status, String result) {
    transactionTemplate.executeWithoutResult(tx -> {
      AgentAction action = actionRepository.findById(actionId).orElseThrow();
      action.setStatus(status);
      action.setResult(result != null && result.length() > AgentConstants.MAX_AUDIT_RESULT_CHARS
        ? result.substring(0, AgentConstants.MAX_AUDIT_RESULT_CHARS) : result);
    });
  }
}
