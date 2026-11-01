package com.expense.tracker.dto;

import java.math.BigDecimal;

public class MonthlyTrendDTO {

    private int month;
    private String monthName;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal savings;

    public MonthlyTrendDTO() {}

    public MonthlyTrendDTO(int month, String monthName, BigDecimal income, BigDecimal expense, BigDecimal savings) {
        this.month = month;
        this.monthName = monthName;
        this.income = income;
        this.expense = expense;
        this.savings = savings;
    }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

    public BigDecimal getIncome() { return income; }
    public void setIncome(BigDecimal income) { this.income = income; }

    public BigDecimal getExpense() { return expense; }
    public void setExpense(BigDecimal expense) { this.expense = expense; }

    public BigDecimal getSavings() { return savings; }
    public void setSavings(BigDecimal savings) { this.savings = savings; }
}
