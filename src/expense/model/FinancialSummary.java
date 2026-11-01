package expense.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Encapsulates computed financial summary metrics for a specified period.
 */
public class FinancialSummary {

    private final BigDecimal totalIncome;
    private final BigDecimal totalExpense;
    private final BigDecimal netBalance;
    private final BigDecimal monthlySavings;
    private final BigDecimal monthlyBudget;
    private final BigDecimal remainingBudget;
    private final double savingsPercentage;
    private final double budgetUtilizationPercentage;
    private final String highestSpendingCategory;
    private final BigDecimal highestSpendingAmount;
    private final int totalTransactionCount;

    public FinancialSummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal monthlyBudget,
                            String highestSpendingCategory, BigDecimal highestSpendingAmount,
                            int totalTransactionCount) {
        this.totalIncome = totalIncome != null ? totalIncome.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.totalExpense = totalExpense != null ? totalExpense.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.netBalance = this.totalIncome.subtract(this.totalExpense).setScale(2, RoundingMode.HALF_UP);
        this.monthlySavings = this.netBalance; // Savings for the period
        this.monthlyBudget = monthlyBudget != null ? monthlyBudget.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        if (this.monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
            this.remainingBudget = this.monthlyBudget.subtract(this.totalExpense).setScale(2, RoundingMode.HALF_UP);
            double util = (this.totalExpense.doubleValue() / this.monthlyBudget.doubleValue()) * 100.0;
            this.budgetUtilizationPercentage = Math.round(util * 100.0) / 100.0;
        } else {
            this.remainingBudget = BigDecimal.ZERO;
            this.budgetUtilizationPercentage = 0.0;
        }

        if (this.totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            double savRatio = (this.monthlySavings.doubleValue() / this.totalIncome.doubleValue()) * 100.0;
            this.savingsPercentage = Math.round(savRatio * 100.0) / 100.0;
        } else {
            this.savingsPercentage = 0.0;
        }

        this.highestSpendingCategory = (highestSpendingCategory != null && !highestSpendingCategory.isBlank()) ?
                highestSpendingCategory : "None";
        this.highestSpendingAmount = highestSpendingAmount != null ?
                highestSpendingAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.totalTransactionCount = totalTransactionCount;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public BigDecimal getMonthlySavings() {
        return monthlySavings;
    }

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public double getSavingsPercentage() {
        return savingsPercentage;
    }

    public double getBudgetUtilizationPercentage() {
        return budgetUtilizationPercentage;
    }

    public String getHighestSpendingCategory() {
        return highestSpendingCategory;
    }

    public BigDecimal getHighestSpendingAmount() {
        return highestSpendingAmount;
    }

    public int getTotalTransactionCount() {
        return totalTransactionCount;
    }
}
