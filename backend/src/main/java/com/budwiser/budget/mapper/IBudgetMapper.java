package com.budwiser.budget.mapper;

import com.budwiser.analytics.engine.BudgetStatus;
import com.budwiser.budget.dto.BudgetDto;
import com.budwiser.budget.entity.Budget;
import com.budwiser.category.entity.Category;
import java.time.YearMonth;
import org.mapstruct.Mapper;

@Mapper
public interface IBudgetMapper {

  /** Three sources (entity, category, computed status) — clearer as an explicit builder than nested @Mappings. */
  default BudgetDto toDto(Budget budget, Category category, BudgetStatus status, YearMonth month) {
    return BudgetDto.builder()
      .id(String.valueOf(budget.getId()))
      .categoryId(String.valueOf(budget.getCategoryId()))
      .categoryName(category.getName())
      .categoryIcon(category.getIcon())
      .amount(status.limit())
      .month(month)
      .spent(status.spent())
      .remaining(status.remaining())
      .percentUsed(status.percentUsed())
      .level(status.level())
      .build();
  }
}
