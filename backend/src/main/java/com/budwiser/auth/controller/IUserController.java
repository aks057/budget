package com.budwiser.auth.controller;

import com.budwiser.auth.dto.UpdateProfileRequest;
import com.budwiser.auth.dto.UserDto;
import com.budwiser.common.response.Response;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public interface IUserController {

  @GetMapping("/me")
  Response<UserDto> getCurrentUser();

  @PutMapping("/me")
  Response<UserDto> updateCurrentUser(@Valid @RequestBody UpdateProfileRequest request);
}
