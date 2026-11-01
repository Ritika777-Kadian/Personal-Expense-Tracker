package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryExpenseDTO;
import com.expense.tracker.dto.FinancialSummaryDTO;
import com.expense.tracker.dto.MonthlyTrendDTO;
import com.expense.tracker.model.Budget;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Transaction;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.BudgetRepository;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public FinancialSummaryDTO getFinancialSummary(User user, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Transaction> allTxns = transactionRepository.findByUserIdOrderByDateDescIdDesc(user.getId());

        // All-time balance
        BigDecimal allTimeBalance = BigDecimal.ZERO;
        for (Transaction t : allTxns) {
            if (t.isIncome()) {
                allTimeBalance = allTimeBalance.add(t.getAmount());
            } else if (t.isExpense()) {
                allTimeBalance = allTimeBalance.subtract(t.getAmount());
            }
        }
        allTimeBalance = allTimeBalance.setScale(2, RoundingMode.HALF_UP);

        // Monthly calculations
        BigDecimal monthlyIncome = BigDecimal.ZERO;
        BigDecimal monthlyExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> catExpenses = new HashMap<>();
        int monthlyTxnCount = 0;

        for (Transaction t : allTxns) {
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                monthlyTxnCount++;
                if (t.isIncome()) {
                    monthlyIncome = monthlyIncome.add(t.getAmount());
                } else if (t.isExpense()) {
                    monthlyExpense = monthlyExpense.add(t.getAmount());
                    String cat = t.getCategory();
                    catExpenses.put(cat, catExpenses.getOrDefault(cat, BigDecimal.ZERO).add(t.getAmount()));
                }
            }
        }

        monthlyIncome = monthlyIncome.setScale(2, RoundingMode.HALF_UP);
        monthlyExpense = monthlyExpense.setScale(2, RoundingMode.HALF_UP);
        BigDecimal netBalance = monthlyIncome.subtract(monthlyExpense).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlySavings = netBalance;

        // Highest spending category
        String highestCat = "None";
        BigDecimal highestAmt = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : catExpenses.entrySet()) {
            if (entry.getValue().compareTo(highestAmt) > 0) {
                highestAmt = entry.getValue();
                highestCat = entry.getKey();
            }
        }

        // Monthly Budget
        BigDecimal monthlyBudget = budgetRepository.findByUserIdAndCategoryIgnoreCaseAndYearAndMonth(
                user.getId(), Budget.OVERALL_CATEGORY, year, month
        ).map(Budget::getMonthlyLimit).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingBudget = BigDecimal.ZERO;
        double budgetUtil = 0.0;
        if (monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
            remainingBudget = monthlyBudget.subtract(monthlyExpense).setScale(2, RoundingMode.HALF_UP);
            budgetUtil = Math.round((monthlyExpense.doubleValue() / monthlyBudget.doubleValue()) * 10000.0) / 100.0;
        }

        double savingsPercentage = 0.0;
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsPercentage = Math.round((monthlySavings.doubleValue() / monthlyIncome.doubleValue()) * 10000.0) / 100.0;
        }

        return new FinancialSummaryDTO(
                monthlyIncome, monthlyExpense, netBalance, monthlySavings,
                monthlyBudget, remainingBudget, savingsPercentage, budgetUtil,
                highestCat, highestAmt, allTimeBalance, monthlyTxnCount
        );
    }

    public List<MonthlyTrendDTO> get12MonthTrends(User user, int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);

        List<Transaction> annualTxns = transactionRepository.findByUserIdAndDateBetweenOrderByDateDescIdDesc(
                user.getId(), startOfYear, endOfYear
        );

        BigDecimal[] incomeArr = new BigDecimal[12];
        BigDecimal[] expenseArr = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            incomeArr[i] = BigDecimal.ZERO;
            expenseArr[i] = BigDecimal.ZERO;
        }

        for (Transaction t : annualTxns) {
            int mIdx = t.getDate().getMonthValue() - 1;
            if (t.isIncome()) {
                incomeArr[mIdx] = incomeArr[mIdx].add(t.getAmount());
            } else if (t.isExpense()) {
                expenseArr[mIdx] = expenseArr[mIdx].add(t.getAmount());
            }
        }

        List<MonthlyTrendDTO> trends = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String monthName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            BigDecimal inc = incomeArr[m - 1].setScale(2, RoundingMode.HALF_UP);
            BigDecimal exp = expenseArr[m - 1].setScale(2, RoundingMode.HALF_UP);
            BigDecimal sav = inc.subtract(exp).setScale(2, RoundingMode.HALF_UP);
            trends.add(new MonthlyTrendDTO(m, monthName, inc, exp, sav));
        }

        return trends;
    }

    public List<CategoryExpenseDTO> getCategoryBreakdown(User user, LocalDate start, LocalDate end) {
        List<Transaction> txns = transactionRepository.findByUserIdAndDateBetweenOrderByDateDescIdDesc(
                user.getId(), start, end
        );

        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> catTotals = new HashMap<>();
        Map<String, Integer> catCounts = new HashMap<>();

        for (Transaction t : txns) {
            if (t.isExpense()) {
                totalExpense = totalExpense.add(t.getAmount());
                String cat = t.getCategory();
                catTotals.put(cat, catTotals.getOrDefault(cat, BigDecimal.ZERO).add(t.getAmount()));
                catCounts.put(cat, catCounts.getOrDefault(cat, 0) + 1);
            }
        }

        // Color lookup from Category entity
        Map<String, String> colorMap = new HashMap<>();
        List<Category> userCategories = categoryRepository.findByUserIdOrderByNameAsc(user.getId());
        for (Category c : userCategories) {
            colorMap.put(c.getName().toLowerCase(), c.getColor());
        }

        List<CategoryExpenseDTO> results = new ArrayList<>();
        String[] fallbackPalette = { "#F59E0B", "#EF4444", "#3B82F6", "#8B5CF6", "#EC4899", "#10B981", "#6366F1", "#14B8A6", "#F97316", "#64748B" };
        int colorIdx = 0;

        for (Map.Entry<String, BigDecimal> entry : catTotals.entrySet()) {
            String catName = entry.getKey();
            BigDecimal amount = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            double pct = 0.0;
            if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                pct = Math.round((amount.doubleValue() / totalExpense.doubleValue()) * 10000.0) / 100.0;
            }

            String color = colorMap.getOrDefault(catName.toLowerCase(), fallbackPalette[colorIdx % fallbackPalette.length]);
            colorIdx++;

            results.add(new CategoryExpenseDTO(catName, amount, pct, color, catCounts.getOrDefault(catName, 0)));
        }

        results.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return results;
    }
}
