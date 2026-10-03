package com.budwiser.category.controller.impl;

import com.budwiser.category.controller.ICategoryController;
import com.budwiser.category.dto.CategoryDto;
import com.budwiser.category.dto.CreateCategoryRequest;
import com.budwiser.category.dto.UpdateCategoryRequest;
import com.budwiser.category.service.ICategoryService;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.response.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryController implements ICategoryController {
  private final ICategoryService categoryService;

  @Override
  public Response<List<CategoryDto>> list(TransactionType type) {
    return Response.<List<CategoryDto>>builder().data(categoryService.list(type)).build();
  }

  @Override
  public Response<CategoryDto> create(CreateCategoryRequest request) {
    return Response.<CategoryDto>builder().data(categoryService.create(request)).build();
  }

  @Override
  public Response<CategoryDto> update(Long id, UpdateCategoryRequest request) {
    return Response.<CategoryDto>builder().data(categoryService.update(id, request)).build();
  }

  @Override
  public Response<Void> delete(Long id) {
    categoryService.delete(id);
    return Response.<Void>builder().build();
  }
}
