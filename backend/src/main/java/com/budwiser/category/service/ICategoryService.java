package com.budwiser.category.service;

import com.budwiser.category.dto.CategoryDto;
import com.budwiser.category.dto.CreateCategoryRequest;
import com.budwiser.category.dto.UpdateCategoryRequest;
import com.budwiser.category.entity.Category;
import com.budwiser.common.constant.TransactionType;
import java.util.List;
import java.util.Map;

public interface ICategoryService {

  List<CategoryDto> list(TransactionType type);

  CategoryDto create(CreateCategoryRequest request);

  CategoryDto update(Long id, UpdateCategoryRequest request);

  void delete(Long id);

  void createDefaultCategories(Long userId);

  /** For other modules: the user's category or 404 (also when it belongs to someone else). */
  Category getOwnedCategory(Long userId, Long categoryId);

  /** All of the user's categories keyed by id — bounded by MAX_CATEGORIES_PER_USER. */
  Map<Long, Category> getCategoriesById(Long userId);
}
