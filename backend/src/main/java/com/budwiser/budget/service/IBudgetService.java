package com.budwiser.budget.service;

import com.budwiser.budget.dto.BudgetDto;
import com.budwiser.budget.dto.CreateBudgetRequest;
import com.budwiser.budget.dto.UpdateBudgetRequest;
import java.time.YearMonth;
import java.util.List;

public interface IBudgetService {

  BudgetDto create(CreateBudgetRequest request);

  /** All budgets with spending status for the month (default: current month). */
  List<BudgetDto> list(YearMonth month);

  /** Same as {@link #list} for an explicit user — for internal callers without a request (scheduler, analytics). */
  List<BudgetDto> getStatuses(Long userId, YearMonth month);

  BudgetDto update(Long id, UpdateBudgetRequest request);

  void delete(Long id);
}
