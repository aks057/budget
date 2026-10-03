package com.budwiser.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  @NotBlank(message = "Email is required")
  @Size(max = 320, message = "Email must be at most 320 characters")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(max = 72, message = "Password must be at most 72 characters")
  private String password;
}
