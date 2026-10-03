package com.budwiser.goal.service;

import com.budwiser.goal.dto.GoalDto;
import com.budwiser.goal.dto.GoalRequest;
import java.util.List;

public interface IGoalService {

  GoalDto create(GoalRequest request);

  List<GoalDto> list();

  GoalDto getById(Long id);

  GoalDto update(Long id, GoalRequest request);

  void delete(Long id);
}
