package com.budwiser.agent.repository;

import com.budwiser.agent.constant.AgentQueries;
import com.budwiser.agent.entity.AgentMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IAgentMessageRepository extends JpaRepository<AgentMessage, Long> {

  @Query(value = AgentQueries.RECENT_MESSAGES, nativeQuery = true)
  List<AgentMessage> findRecent(@Param("conversationId") Long conversationId,
                                @Param("userId") Long userId,
                                @Param("limit") int limit);
}
