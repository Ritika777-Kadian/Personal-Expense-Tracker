package com.expense.tracker.dto;

import java.math.BigDecimal;

public class BudgetStatusDTO {

    private Long id;
    private String category;
    private BigDecimal limit;
    private BigDecimal spent;
    private BigDecimal remaining;
    private double utilizationPercentage;
    private String alertLevel; // NORMAL, WARNING_80, EXCEEDED
    private boolean warning;
    private boolean exceeded;

    public BudgetStatusDTO() {}

    public BudgetStatusDTO(Long id, String category, BigDecimal limit, BigDecimal spent,
                           BigDecimal remaining, double utilizationPercentage, String alertLevel) {
        this.id = id;
        this.category = category;
        this.limit = limit;
        this.spent = spent;
        this.remaining = remaining;
        this.utilizationPercentage = utilizationPercentage;
        this.alertLevel = alertLevel;
        this.warning = "WARNING_80".equalsIgnoreCase(alertLevel);
        this.exceeded = "EXCEEDED".equalsIgnoreCase(alertLevel);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getLimit() { return limit; }
    public void setLimit(BigDecimal limit) { this.limit = limit; }

    public BigDecimal getSpent() { return spent; }
    public void setSpent(BigDecimal spent) { this.spent = spent; }

    public BigDecimal getRemaining() { return remaining; }
    public void setRemaining(BigDecimal remaining) { this.remaining = remaining; }

    public double getUtilizationPercentage() { return utilizationPercentage; }
    public void setUtilizationPercentage(double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public boolean isWarning() { return warning; }
    public void setWarning(boolean warning) { this.warning = warning; }

    public boolean isExceeded() { return exceeded; }
    public void setExceeded(boolean exceeded) { this.exceeded = exceeded; }
}
