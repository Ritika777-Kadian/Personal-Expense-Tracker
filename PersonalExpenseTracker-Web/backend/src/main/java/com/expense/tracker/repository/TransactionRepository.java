package com.expense.tracker.repository;

import com.expense.tracker.model.Transaction;
import com.expense.tracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByDateDescIdDesc(Long userId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findByUserIdAndDateBetweenOrderByDateDescIdDesc(Long userId, LocalDate startDate, LocalDate endDate);

    long countByUserIdAndCategoryIgnoreCase(Long userId, String category);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND " +
           "(:query IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.category) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:category IS NULL OR LOWER(t.category) = LOWER(:category)) AND " +
           "(:startDate IS NULL OR t.date >= :startDate) AND " +
           "(:endDate IS NULL OR t.date <= :endDate) AND " +
           "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR t.amount <= :maxAmount) " +
           "ORDER BY t.date DESC, t.id DESC")
    List<Transaction> searchTransactions(@Param("userId") Long userId,
                                         @Param("query") String query,
                                         @Param("type") TransactionType type,
                                         @Param("category") String category,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("minAmount") BigDecimal minAmount,
                                         @Param("maxAmount") BigDecimal maxAmount);

    List<Transaction> findByUserIdAndIsDemoTrue(Long userId);

    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.user.id = :userId AND t.isDemo = true")
    int deleteByUserIdAndIsDemoTrue(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Transaction t SET t.category = :newCategory WHERE t.user.id = :userId AND LOWER(t.category) = LOWER(:oldCategory)")
    int updateCategoryName(@Param("userId") Long userId, @Param("oldCategory") String oldCategory, @Param("newCategory") String newCategory);
}
