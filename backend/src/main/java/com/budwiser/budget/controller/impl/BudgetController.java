package com.budwiser.budget.controller.impl;

import com.budwiser.budget.controller.IBudgetController;
import com.budwiser.budget.dto.BudgetDto;
import com.budwiser.budget.dto.CreateBudgetRequest;
import com.budwiser.budget.dto.UpdateBudgetRequest;
import com.budwiser.budget.service.IBudgetService;
import com.budwiser.common.response.Response;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BudgetController implements IBudgetController {
  private final IBudgetService budgetService;

  @Override
  public Response<BudgetDto> create(CreateBudgetRequest request) {
    return Response.<BudgetDto>builder().data(budgetService.create(request)).build();
  }

  @Override
  public Response<List<BudgetDto>> list(YearMonth month) {
    return Response.<List<BudgetDto>>builder().data(budgetService.list(month)).build();
  }

  @Override
  public Response<BudgetDto> update(Long id, UpdateBudgetRequest request) {
    return Response.<BudgetDto>builder().data(budgetService.update(id, request)).build();
  }

  @Override
  public Response<Void> delete(Long id) {
    budgetService.delete(id);
    return Response.<Void>builder().build();
  }
}
