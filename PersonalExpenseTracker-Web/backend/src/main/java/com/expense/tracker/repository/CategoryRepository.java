package com.expense.tracker.repository;

import com.expense.tracker.model.Category;
import com.expense.tracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrderByNameAsc(Long userId);
    List<Category> findByUserIdAndTypeOrderByNameAsc(Long userId, TransactionType type);
    Optional<Category> findByUserIdAndNameIgnoreCaseAndType(Long userId, String name, TransactionType type);
    boolean existsByUserIdAndNameIgnoreCaseAndType(Long userId, String name, TransactionType type);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
}
