package com.budwiser.category.dto;

import com.budwiser.common.constant.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDto {
  private String id;
  private String name;
  private String icon;
  private TransactionType type;
}
