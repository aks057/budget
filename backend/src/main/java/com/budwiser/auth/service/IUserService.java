package com.budwiser.auth.service;

import com.budwiser.auth.dto.UpdateProfileRequest;
import com.budwiser.auth.dto.UserDto;

public interface IUserService {

  UserDto getCurrentUser();

  UserDto updateCurrentUser(UpdateProfileRequest request);
}
