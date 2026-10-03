package com.budwiser.auth.repository;

import com.budwiser.auth.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByGoogleSubject(String googleSubject);

  boolean existsByEmail(String email);

  /** Keyset page of user ids (stable while users register mid-run, unlike OFFSET paging). */
  @Query("SELECT u.id FROM User u WHERE u.id > :afterId ORDER BY u.id")
  List<Long> findIdsAfter(@Param("afterId") Long afterId, Pageable pageable);
}
