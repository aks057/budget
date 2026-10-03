package com.budwiser.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

  @NotBlank(message = "Full name is required")
  @Size(max = 120, message = "Full name must be at most 120 characters")
  private String fullName;

  /** Same list as the frontend's lib/currencies.ts. */
  @NotBlank(message = "Currency is required")
  @Pattern(regexp = "INR|USD|EUR|GBP", message = "Currency must be one of INR, USD, EUR, GBP")
  private String currency;
}
