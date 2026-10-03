package com.budwiser.category.controller;

import com.budwiser.category.dto.CategoryDto;
import com.budwiser.category.dto.CreateCategoryRequest;
import com.budwiser.category.dto.UpdateCategoryRequest;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.response.Response;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public interface ICategoryController {

  /** Not paginated: bounded by MAX_CATEGORIES_PER_USER. */
  @GetMapping
  Response<List<CategoryDto>> list(@RequestParam(required = false) TransactionType type);

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  Response<CategoryDto> create(@Valid @RequestBody CreateCategoryRequest request);

  @PutMapping("/{id}")
  Response<CategoryDto> update(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request);

  @DeleteMapping("/{id}")
  Response<Void> delete(@PathVariable Long id);
}
