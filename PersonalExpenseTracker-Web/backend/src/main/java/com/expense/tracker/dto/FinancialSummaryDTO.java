package com.expense.tracker.dto;

import java.math.BigDecimal;

public class FinancialSummaryDTO {

    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal netBalance;
    private BigDecimal monthlySavings;
    private BigDecimal monthlyBudget;
    private BigDecimal remainingBudget;
    private double savingsPercentage;
    private double budgetUtilizationPercentage;
    private String highestSpendingCategory;
    private BigDecimal highestSpendingAmount;
    private BigDecimal allTimeBalance;
    private int totalTransactions;

    public FinancialSummaryDTO() {}

    public FinancialSummaryDTO(BigDecimal monthlyIncome, BigDecimal monthlyExpense, BigDecimal netBalance,
                               BigDecimal monthlySavings, BigDecimal monthlyBudget, BigDecimal remainingBudget,
                               double savingsPercentage, double budgetUtilizationPercentage,
                               String highestSpendingCategory, BigDecimal highestSpendingAmount,
                               BigDecimal allTimeBalance, int totalTransactions) {
        this.monthlyIncome = monthlyIncome;
        this.monthlyExpense = monthlyExpense;
        this.netBalance = netBalance;
        this.monthlySavings = monthlySavings;
        this.monthlyBudget = monthlyBudget;
        this.remainingBudget = remainingBudget;
        this.savingsPercentage = savingsPercentage;
        this.budgetUtilizationPercentage = budgetUtilizationPercentage;
        this.highestSpendingCategory = highestSpendingCategory;
        this.highestSpendingAmount = highestSpendingAmount;
        this.allTimeBalance = allTimeBalance;
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getMonthlyExpense() { return monthlyExpense; }
    public void setMonthlyExpense(BigDecimal monthlyExpense) { this.monthlyExpense = monthlyExpense; }

    public BigDecimal getNetBalance() { return netBalance; }
    public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }

    public BigDecimal getMonthlySavings() { return monthlySavings; }
    public void setMonthlySavings(BigDecimal monthlySavings) { this.monthlySavings = monthlySavings; }

    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public BigDecimal getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }

    public double getSavingsPercentage() { return savingsPercentage; }
    public void setSavingsPercentage(double savingsPercentage) { this.savingsPercentage = savingsPercentage; }

    public double getBudgetUtilizationPercentage() { return budgetUtilizationPercentage; }
    public void setBudgetUtilizationPercentage(double budgetUtilizationPercentage) { this.budgetUtilizationPercentage = budgetUtilizationPercentage; }

    public String getHighestSpendingCategory() { return highestSpendingCategory; }
    public void setHighestSpendingCategory(String highestSpendingCategory) { this.highestSpendingCategory = highestSpendingCategory; }

    public BigDecimal getHighestSpendingAmount() { return highestSpendingAmount; }
    public void setHighestSpendingAmount(BigDecimal highestSpendingAmount) { this.highestSpendingAmount = highestSpendingAmount; }

    public BigDecimal getAllTimeBalance() { return allTimeBalance; }
    public void setAllTimeBalance(BigDecimal allTimeBalance) { this.allTimeBalance = allTimeBalance; }

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
}
