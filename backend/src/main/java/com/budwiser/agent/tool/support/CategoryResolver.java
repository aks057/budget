package com.budwiser.agent.tool.support;

import com.budwiser.category.entity.Category;
import com.budwiser.category.service.ICategoryService;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.exception.Error;
import com.budwiser.common.exception.ValidationException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Models speak in category names ("Food"), the domain in ids. Resolution is exact (case-insensitive) — no fuzzy
 * guessing: an unknown name is rejected with the list of valid names so the model can retry correctly.
 */
@Component
@RequiredArgsConstructor
public class CategoryResolver {
  private final ICategoryService categoryService;

  public Category resolve(Long userId, String name, TransactionType type) {
    String key = name.trim().toLowerCase(Locale.ROOT);
    Collection<Category> all = categoryService.getCategoriesById(userId).values();
    List<Category> matches = all.stream()
      .filter(category -> category.getNameKey().equals(key))
      .filter(category -> type == null || category.getType() == type)
      .toList();
    if (matches.size() == 1) {
      return matches.getFirst();
    }
    if (matches.isEmpty()) {
      String available = all.stream()
        .filter(category -> type == null || category.getType() == type)
        .map(Category::getName)
        .sorted()
        .collect(Collectors.joining(", "));
      throw new ValidationException("Unknown category '" + name + "'. Valid names: " + available,
        List.of(Error.of(ErrorCode.CATEGORY_NOT_FOUND, ExceptionType.VALIDATION_ERROR)));
    }
    throw new ValidationException("Category '" + name + "' exists as both income and expense; pass type",
      List.of(Error.of(ErrorCode.VALIDATION_FAILED, ExceptionType.VALIDATION_ERROR)));
  }
}
