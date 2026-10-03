package com.budwiser.auth.service;

import com.budwiser.auth.dto.LoginRequest;
import com.budwiser.auth.dto.RegisterRequest;
import com.budwiser.auth.model.AuthResult;
import com.budwiser.auth.model.GoogleProfile;

public interface IAuthService {

  AuthResult register(RegisterRequest request, String userAgent);

  AuthResult login(LoginRequest request, String userAgent);

  AuthResult loginWithGoogle(GoogleProfile profile, String userAgent);

  AuthResult refresh(String rawRefreshToken, String userAgent);

  void logout(String rawRefreshToken);
}
