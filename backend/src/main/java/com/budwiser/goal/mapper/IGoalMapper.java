package com.budwiser.goal.mapper;

import com.budwiser.analytics.engine.GoalProgress;
import com.budwiser.goal.dto.GoalDto;
import com.budwiser.goal.entity.FinancialGoal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface IGoalMapper {

  @Mapping(target = "id", source = "goal.id")
  GoalDto toDto(FinancialGoal goal, GoalProgress progress);
}
