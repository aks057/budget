package com.budwiser.insight.repository;

import com.budwiser.insight.constant.InsightQueries;
import com.budwiser.insight.entity.Insight;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IInsightRepository extends JpaRepository<Insight, Long> {

  Optional<Insight> findByIdAndUserId(Long id, Long userId);

  List<Insight> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

  long countByUserIdAndReadAtIsNull(Long userId);

  /** @return 1 when inserted, 0 when an insight with this dedupe key already exists for the user */
  @Modifying
  @Query(value = InsightQueries.INSERT_IF_ABSENT, nativeQuery = true)
  int insertIfAbsent(@Param("userId") Long userId, @Param("type") String type, @Param("severity") String severity,
                     @Param("title") String title, @Param("body") String body, @Param("dedupeKey") String dedupeKey,
                     @Param("now") long now);

  @Modifying
  @Query(value = InsightQueries.MARK_ALL_READ, nativeQuery = true)
  int markAllRead(@Param("userId") Long userId, @Param("now") long now);

  @Modifying
  @Query(value = InsightQueries.DELETE_CREATED_BEFORE, nativeQuery = true)
  int deleteCreatedBefore(@Param("cutoff") long cutoff);
}
