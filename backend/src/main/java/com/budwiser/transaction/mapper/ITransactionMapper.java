package com.budwiser.transaction.mapper;

import com.budwiser.category.entity.Category;
import com.budwiser.transaction.dto.TransactionDto;
import com.budwiser.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ITransactionMapper {

  @Mapping(target = "id", source = "transaction.id")
  @Mapping(target = "type", source = "transaction.type")
  @Mapping(target = "categoryId", source = "transaction.categoryId")
  @Mapping(target = "categoryName", source = "category.name")
  @Mapping(target = "categoryIcon", source = "category.icon")
  TransactionDto toDto(Transaction transaction, Category category);
}
