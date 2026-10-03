package com.budwiser.auth.service;

import com.budwiser.auth.model.IssuedRefreshToken;
import com.budwiser.auth.model.RefreshRotation;

public interface IRefreshTokenService {

  /** Starts a new token family (one per login session). */
  IssuedRefreshToken issue(Long userId, String userAgent);

  /** Revokes the presented token and issues its successor in the same family; reuse of a revoked token kills the family. */
  RefreshRotation rotate(String rawToken, String userAgent);

  /** Logout: revokes every token of the presented token's family. Unknown tokens are ignored (idempotent). */
  void revokeFamily(String rawToken);
}
