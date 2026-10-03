package com.budwiser.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Type is immutable: changing it would silently re-classify every existing transaction in the category.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

  @NotBlank(message = "Name is required")
  @Size(max = 50, message = "Name must be at most 50 characters")
  private String name;

  @Size(max = 16, message = "Icon must be at most 16 characters")
  private String icon;
}
