package com.budwiser.auth.controller.impl;

import com.budwiser.auth.controller.IUserController;
import com.budwiser.auth.dto.UpdateProfileRequest;
import com.budwiser.auth.dto.UserDto;
import com.budwiser.auth.service.IUserService;
import com.budwiser.common.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserController implements IUserController {
  private final IUserService userService;

  @Override
  public Response<UserDto> getCurrentUser() {
    return Response.<UserDto>builder().data(userService.getCurrentUser()).build();
  }

  @Override
  public Response<UserDto> updateCurrentUser(UpdateProfileRequest request) {
    return Response.<UserDto>builder().data(userService.updateCurrentUser(request)).build();
  }
}
