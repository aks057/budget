package com.budwiser.category.mapper;

import com.budwiser.category.dto.CategoryDto;
import com.budwiser.category.entity.Category;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper
public interface ICategoryMapper {

  CategoryDto toDto(Category category);

  List<CategoryDto> toDtos(List<Category> categories);
}
