package com.budwiser.auth.repository;

import com.budwiser.auth.constant.AuthQueries;
import com.budwiser.auth.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = AuthQueries.REVOKE_REFRESH_TOKEN_FAMILY, nativeQuery = true)
  int revokeFamily(@Param("familyId") UUID familyId, @Param("now") long now);
}
