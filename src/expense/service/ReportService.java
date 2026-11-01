package expense.service;

import expense.model.*;
import expense.util.DateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Business service responsible for analytical computations, summaries,
 * aggregations, and trend generation.
 */
public class ReportService {

    public static class MonthlyTrendPoint {
        private final int month;
        private final String monthName;
        private final BigDecimal income;
        private final BigDecimal expense;
        private final BigDecimal savings;

        public MonthlyTrendPoint(int month, String monthName, BigDecimal income, BigDecimal expense) {
            this.month = month;
            this.monthName = monthName;
            this.income = income != null ? income.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            this.expense = expense != null ? expense.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            this.savings = this.income.subtract(this.expense).setScale(2, RoundingMode.HALF_UP);
        }

        public int getMonth() { return month; }
        public String getMonthName() { return monthName; }
        public BigDecimal getIncome() { return income; }
        public BigDecimal getExpense() { return expense; }
        public BigDecimal getSavings() { return savings; }
    }

    public ReportService() {}

    /**
     * Calculates the all-time net current balance across all recorded transactions.
     */
    public BigDecimal getAllTimeBalance(TransactionService txnService) {
        BigDecimal balance = BigDecimal.ZERO;
        if (txnService != null) {
            for (Transaction t : txnService.getAllTransactions()) {
                if (t.isIncome()) {
                    balance = balance.add(t.getAmount());
                } else if (t.isExpense()) {
                    balance = balance.subtract(t.getAmount());
                }
            }
        }
        return balance.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates FinancialSummary for the specified month and year.
     */
    public FinancialSummary getMonthlySummary(int year, int month, TransactionService txnService, BudgetService budgetService) {
        LocalDate start = DateUtil.getStartOfMonth(year, month);
        LocalDate end = DateUtil.getEndOfMonth(year, month);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        Map<String, BigDecimal> catExpenses = new HashMap<>();
        int count = 0;

        for (Transaction t : txnService.getAllTransactions()) {
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                count++;
                if (t.isIncome()) {
                    income = income.add(t.getAmount());
                } else if (t.isExpense()) {
                    expense = expense.add(t.getAmount());
                    String cat = t.getCategory();
                    catExpenses.put(cat, catExpenses.getOrDefault(cat, BigDecimal.ZERO).add(t.getAmount()));
                }
            }
        }

        // Determine highest spending category
        String highestCat = "None";
        BigDecimal highestAmt = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : catExpenses.entrySet()) {
            if (entry.getValue().compareTo(highestAmt) > 0) {
                highestAmt = entry.getValue();
                highestCat = entry.getKey();
            }
        }

        // Monthly budget from BudgetService
        BigDecimal monthlyBudget = budgetService.getOverallBudget(year, month)
                .map(Budget::getMonthlyLimit)
                .orElse(BigDecimal.ZERO);

        return new FinancialSummary(income, expense, monthlyBudget, highestCat, highestAmt, count);
    }

    /**
     * Aggregates expenses by category for a date range.
     */
    public Map<String, BigDecimal> getExpenseCategoryBreakdown(LocalDate start, LocalDate end, TransactionService txnService) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Transaction t : txnService.getAllTransactions()) {
            if (t.isExpense() && DateUtil.isDateInRange(t.getDate(), start, end)) {
                String cat = t.getCategory();
                map.put(cat, map.getOrDefault(cat, BigDecimal.ZERO).add(t.getAmount()));
            }
        }
        return map;
    }

    /**
     * Aggregates income by category for a date range.
     */
    public Map<String, BigDecimal> getIncomeCategoryBreakdown(LocalDate start, LocalDate end, TransactionService txnService) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Transaction t : txnService.getAllTransactions()) {
            if (t.isIncome() && DateUtil.isDateInRange(t.getDate(), start, end)) {
                String cat = t.getCategory();
                map.put(cat, map.getOrDefault(cat, BigDecimal.ZERO).add(t.getAmount()));
            }
        }
        return map;
    }

    /**
     * Calculates 12-month trend of Income and Expenses for the given year.
     */
    public List<MonthlyTrendPoint> getYearlyTrend(int year, TransactionService txnService) {
        List<MonthlyTrendPoint> list = new ArrayList<>();
        BigDecimal[] monthlyIncome = new BigDecimal[13];
        BigDecimal[] monthlyExpense = new BigDecimal[13];

        for (int i = 1; i <= 12; i++) {
            monthlyIncome[i] = BigDecimal.ZERO;
            monthlyExpense[i] = BigDecimal.ZERO;
        }

        for (Transaction t : txnService.getAllTransactions()) {
            if (t.getDate().getYear() == year) {
                int m = t.getDate().getMonthValue();
                if (t.isIncome()) {
                    monthlyIncome[m] = monthlyIncome[m].add(t.getAmount());
                } else if (t.isExpense()) {
                    monthlyExpense[m] = monthlyExpense[m].add(t.getAmount());
                }
            }
        }

        for (int m = 1; m <= 12; m++) {
            String mName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            list.add(new MonthlyTrendPoint(m, mName, monthlyIncome[m], monthlyExpense[m]));
        }

        return list;
    }

    public List<Transaction> getRecentTransactions(int limit, TransactionService txnService) {
        List<Transaction> all = txnService.getAllTransactions();
        if (all.size() <= limit) {
            return all;
        }
        return new ArrayList<>(all.subList(0, limit));
    }
}
