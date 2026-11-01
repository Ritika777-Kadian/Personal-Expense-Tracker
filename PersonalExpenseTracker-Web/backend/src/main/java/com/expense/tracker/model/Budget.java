package com.expense.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category", "budget_year", "budget_month"})
})
public class Budget {

    public static final String OVERALL_CATEGORY = "Overall";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String category = OVERALL_CATEGORY;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "budget_year", nullable = false)
    private int year;

    @Column(name = "budget_month", nullable = false)
    private int month;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Budget() {}

    public Budget(String category, BigDecimal monthlyLimit, int year, int month, User user) {
        this.category = (category == null || category.isBlank()) ? OVERALL_CATEGORY : category.trim();
        this.monthlyLimit = monthlyLimit != null ? monthlyLimit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.year = year;
        this.month = month;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.monthlyLimit != null) {
            this.monthlyLimit = this.monthlyLimit.setScale(2, RoundingMode.HALF_UP);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.monthlyLimit != null) {
            this.monthlyLimit = this.monthlyLimit.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public boolean isOverall() {
        return OVERALL_CATEGORY.equalsIgnoreCase(category);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = (category == null || category.isBlank()) ? OVERALL_CATEGORY : category.trim();
    }

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit != null ? monthlyLimit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
