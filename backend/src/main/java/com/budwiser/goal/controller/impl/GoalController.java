package com.budwiser.goal.controller.impl;

import com.budwiser.common.response.Response;
import com.budwiser.goal.controller.IGoalController;
import com.budwiser.goal.dto.GoalDto;
import com.budwiser.goal.dto.GoalRequest;
import com.budwiser.goal.service.IGoalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalController implements IGoalController {
  private final IGoalService goalService;

  @Override
  public Response<GoalDto> create(GoalRequest request) {
    return Response.<GoalDto>builder().data(goalService.create(request)).build();
  }

  @Override
  public Response<List<GoalDto>> list() {
    return Response.<List<GoalDto>>builder().data(goalService.list()).build();
  }

  @Override
  public Response<GoalDto> getById(Long id) {
    return Response.<GoalDto>builder().data(goalService.getById(id)).build();
  }

  @Override
  public Response<GoalDto> update(Long id, GoalRequest request) {
    return Response.<GoalDto>builder().data(goalService.update(id, request)).build();
  }

  @Override
  public Response<Void> delete(Long id) {
    goalService.delete(id);
    return Response.<Void>builder().build();
  }
}
