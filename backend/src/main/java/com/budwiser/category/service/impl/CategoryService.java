package com.budwiser.category.service.impl;

import com.budwiser.category.constant.CategoryConstants;
import com.budwiser.category.dto.CategoryDto;
import com.budwiser.category.dto.CreateCategoryRequest;
import com.budwiser.category.dto.UpdateCategoryRequest;
import com.budwiser.category.entity.Category;
import com.budwiser.category.mapper.ICategoryMapper;
import com.budwiser.category.repository.ICategoryRepository;
import com.budwiser.category.service.ICategoryService;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.exception.ConflictException;
import com.budwiser.common.exception.ResourceNotFoundException;
import com.budwiser.common.exception.ValidationException;
import com.budwiser.security.service.ICurrentUserProvider;
import com.budwiser.transaction.repository.ITransactionRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
  private static final String DEFAULT_ICON = "";

  private final ICategoryRepository categoryRepository;
  private final ITransactionRepository transactionRepository;
  private final ICategoryMapper categoryMapper;
  private final ICurrentUserProvider currentUserProvider;

  @Override
  @Transactional(readOnly = true)
  public List<CategoryDto> list(TransactionType type) {
    Long userId = currentUserProvider.getUserId();
    List<Category> categories = type == null
      ? categoryRepository.findByUserIdOrderByTypeAscNameAsc(userId)
      : categoryRepository.findByUserIdAndTypeOrderByNameAsc(userId, type);
    return categoryMapper.toDtos(categories);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public CategoryDto create(CreateCategoryRequest request) {
    Long userId = currentUserProvider.getUserId();
    if (categoryRepository.countByUserId(userId) >= CategoryConstants.MAX_CATEGORIES_PER_USER) {
      throw new ValidationException(ErrorCode.CATEGORY_LIMIT_REACHED);
    }
    String name = request.getName().trim();
    ensureNameAvailable(userId, request.getType(), name);

    Category category = new Category();
    category.setUserId(userId);
    category.setType(request.getType());
    applyNameAndIcon(category, name, request.getIcon());
    categoryRepository.save(category);
    log.info("[create] category created, userId: {}, categoryId: {}", userId, category.getId());
    return categoryMapper.toDto(category);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public CategoryDto update(Long id, UpdateCategoryRequest request) {
    Long userId = currentUserProvider.getUserId();
    Category category = getOwnedCategory(userId, id);
    String name = request.getName().trim();
    if (!category.getNameKey().equals(nameKey(name))) {
      ensureNameAvailable(userId, category.getType(), name);
    }
    applyNameAndIcon(category, name, request.getIcon());
    return categoryMapper.toDto(category);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id) {
    Long userId = currentUserProvider.getUserId();
    Category category = getOwnedCategory(userId, id);
    if (transactionRepository.existsByUserIdAndCategoryId(userId, id)) {
      throw new ConflictException(ErrorCode.CATEGORY_IN_USE);
    }
    categoryRepository.delete(category);
    log.info("[delete] category deleted, userId: {}, categoryId: {}", userId, id);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void createDefaultCategories(Long userId) {
    List<Category> defaults = CategoryConstants.DEFAULT_CATEGORIES.stream()
      .map(template -> {
        Category category = new Category();
        category.setUserId(userId);
        category.setType(template.type());
        applyNameAndIcon(category, template.name(), template.icon());
        return category;
      })
      .toList();
    categoryRepository.saveAll(defaults);
    log.info("[createDefaultCategories] seeded default categories, userId: {}, count: {}", userId, defaults.size());
  }

  @Override
  @Transactional(readOnly = true)
  public Category getOwnedCategory(Long userId, Long categoryId) {
    return categoryRepository.findByIdAndUserId(categoryId, userId)
      .orElseThrow(() -> new ResourceNotFoundException(categoryId, ErrorCode.CATEGORY_NOT_FOUND));
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, Category> getCategoriesById(Long userId) {
    return categoryRepository.findByUserIdOrderByTypeAscNameAsc(userId).stream()
      .collect(Collectors.toMap(Category::getId, Function.identity()));
  }

  private void ensureNameAvailable(Long userId, TransactionType type, String name) {
    if (categoryRepository.existsByUserIdAndTypeAndNameKey(userId, type, nameKey(name))) {
      throw new ConflictException(ErrorCode.CATEGORY_ALREADY_EXISTS);
    }
  }

  private static void applyNameAndIcon(Category category, String name, String icon) {
    category.setName(name);
    category.setNameKey(nameKey(name));
    category.setIcon(StringUtils.hasText(icon) ? icon.trim() : DEFAULT_ICON);
  }

  private static String nameKey(String name) {
    return name.trim().toLowerCase(Locale.ROOT);
  }
}
