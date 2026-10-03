package com.budwiser.category.repository;

import com.budwiser.category.entity.Category;
import com.budwiser.common.constant.TransactionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByIdAndUserId(Long id, Long userId);

  List<Category> findByUserIdOrderByTypeAscNameAsc(Long userId);

  List<Category> findByUserIdAndTypeOrderByNameAsc(Long userId, TransactionType type);

  boolean existsByUserIdAndTypeAndNameKey(Long userId, TransactionType type, String nameKey);

  long countByUserId(Long userId);
}
