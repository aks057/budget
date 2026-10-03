package com.budwiser.agent.repository;

import com.budwiser.agent.constant.ActionStatus;
import com.budwiser.agent.entity.AgentAction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAgentActionRepository extends JpaRepository<AgentAction, Long> {

  Optional<AgentAction> findByIdAndUserId(Long id, Long userId);

  List<AgentAction> findByUserIdAndConversationIdAndStatusOrderByIdAsc(Long userId, Long conversationId,
                                                                     ActionStatus status);
}
