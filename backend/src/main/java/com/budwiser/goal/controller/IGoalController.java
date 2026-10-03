package com.budwiser.goal.controller;

import com.budwiser.common.response.Response;
import com.budwiser.goal.dto.GoalDto;
import com.budwiser.goal.dto.GoalRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/goals")
public interface IGoalController {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  Response<GoalDto> create(@Valid @RequestBody GoalRequest request);

  /** Not paginated: bounded by MAX_GOALS_PER_USER. */
  @GetMapping
  Response<List<GoalDto>> list();

  @GetMapping("/{id}")
  Response<GoalDto> getById(@PathVariable Long id);

  @PutMapping("/{id}")
  Response<GoalDto> update(@PathVariable Long id, @Valid @RequestBody GoalRequest request);

  @DeleteMapping("/{id}")
  Response<Void> delete(@PathVariable Long id);
}
