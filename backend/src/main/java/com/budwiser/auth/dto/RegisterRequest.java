package com.budwiser.auth.dto;

import jakarta.validation.constraints.Email;
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
public class RegisterRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 320, message = "Email must be at most 320 characters")
  private String email;

  /** BCrypt only uses the first 72 bytes, so longer passwords are rejected rather than silently truncated. */
  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
  private String password;

  @NotBlank(message = "Full name is required")
  @Size(max = 120, message = "Full name must be at most 120 characters")
  private String fullName;
}
