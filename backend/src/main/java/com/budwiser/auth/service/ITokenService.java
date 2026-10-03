package com.budwiser.auth.service;

import com.budwiser.auth.entity.User;
import com.budwiser.auth.model.IssuedAccessToken;

public interface ITokenService {

  IssuedAccessToken issueAccessToken(User user);
}
