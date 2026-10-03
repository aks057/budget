package com.budwiser.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
  private String id;
  private String email;
  private String fullName;
  private String avatarUrl;
  private String currency;
}
