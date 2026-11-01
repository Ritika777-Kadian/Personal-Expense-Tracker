package com.expense.tracker.repository;

import com.expense.tracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndYearAndMonth(Long userId, int year, int month);

    Optional<Budget> findByUserIdAndCategoryIgnoreCaseAndYearAndMonth(Long userId, String category, int year, int month);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE Budget b SET b.category = :newCategory WHERE b.user.id = :userId AND LOWER(b.category) = LOWER(:oldCategory)")
    int updateCategoryName(@Param("userId") Long userId, @Param("oldCategory") String oldCategory, @Param("newCategory") String newCategory);

    @Modifying
    @Query("DELETE FROM Budget b WHERE b.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
