package com.budwiser.budget.controller;

import com.budwiser.budget.dto.BudgetDto;
import com.budwiser.budget.dto.CreateBudgetRequest;
import com.budwiser.budget.dto.UpdateBudgetRequest;
import com.budwiser.common.response.Response;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/budgets")
public interface IBudgetController {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  Response<BudgetDto> create(@Valid @RequestBody CreateBudgetRequest request);

  /** ?month=2026-10 (default: current month). Not paginated: one budget per expense category at most. */
  @GetMapping
  Response<List<BudgetDto>> list(@RequestParam(required = false) YearMonth month);

  @PutMapping("/{id}")
  Response<BudgetDto> update(@PathVariable Long id, @Valid @RequestBody UpdateBudgetRequest request);

  @DeleteMapping("/{id}")
  Response<Void> delete(@PathVariable Long id);
}
