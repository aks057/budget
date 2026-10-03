package com.budwiser.category.dto;

import com.budwiser.common.constant.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

  @NotBlank(message = "Name is required")
  @Size(max = 50, message = "Name must be at most 50 characters")
  private String name;

  @Size(max = 16, message = "Icon must be at most 16 characters")
  private String icon;

  @NotNull(message = "Type is required")
  private TransactionType type;
}
