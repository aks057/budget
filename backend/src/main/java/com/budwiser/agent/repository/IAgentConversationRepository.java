package com.budwiser.agent.repository;

import com.budwiser.agent.entity.AgentConversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAgentConversationRepository extends JpaRepository<AgentConversation, Long> {

  Optional<AgentConversation> findByIdAndUserId(Long id, Long userId);

  List<AgentConversation> findByUserIdOrderByLastMessageAtDesc(Long userId, Pageable pageable);
}
