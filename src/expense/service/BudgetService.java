package expense.service;

import expense.exception.StorageException;
import expense.exception.ValidationException;
import expense.model.Budget;
import expense.model.BudgetAlertLevel;
import expense.model.BudgetStatus;
import expense.model.Transaction;
import expense.storage.BudgetStorage;
import expense.util.ValidationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business service managing monthly and category budgets, spending comparisons,
 * and threshold alert evaluations.
 */
public class BudgetService {

    private final BudgetStorage storage;
    private final List<Budget> budgets;

    public BudgetService(BudgetStorage storage) throws StorageException {
        this.storage = storage;
        this.budgets = new ArrayList<>();
        loadBudgets();
    }

    private void loadBudgets() throws StorageException {
        budgets.clear();
        budgets.addAll(storage.loadAll());
        Collections.sort(budgets);
    }

    public synchronized List<Budget> getAllBudgets() {
        return new ArrayList<>(budgets);
    }

    public synchronized List<Budget> getBudgetsForMonth(int year, int month) {
        return budgets.stream()
                .filter(b -> b.getYear() == year && b.getMonth() == month)
                .sorted()
                .collect(Collectors.toList());
    }

    public synchronized Optional<Budget> getOverallBudget(int year, int month) {
        return budgets.stream()
                .filter(b -> b.getYear() == year && b.getMonth() == month && b.isOverall())
                .findFirst();
    }

    public synchronized Optional<Budget> getCategoryBudget(String category, int year, int month) {
        String catTrim = (category == null || category.isBlank()) ? Budget.OVERALL_CATEGORY : category.trim();
        return budgets.stream()
                .filter(b -> b.getYear() == year && b.getMonth() == month && b.getCategory().equalsIgnoreCase(catTrim))
                .findFirst();
    }

    public synchronized Budget setBudget(String category, BigDecimal limit, int year, int month)
            throws ValidationException, StorageException {
        ValidationUtil.validatePositiveAmount("Budget Limit", limit);
        if (month < 1 || month > 12) {
            throw new ValidationException("Month", "Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new ValidationException("Year", "Year must be between 2000 and 2100");
        }

        String catTrim = (category == null || category.isBlank()) ? Budget.OVERALL_CATEGORY : category.trim();

        // Check if existing
        Optional<Budget> existingOpt = getCategoryBudget(catTrim, year, month);
        Budget budget;
        if (existingOpt.isPresent()) {
            budget = existingOpt.get();
            budget.setMonthlyLimit(limit);
        } else {
            budget = new Budget(catTrim, limit, year, month);
            budgets.add(budget);
        }

        Collections.sort(budgets);
        storage.saveAll(budgets);
        return budget;
    }

    public synchronized boolean deleteBudget(String category, int year, int month) throws StorageException {
        String catTrim = (category == null || category.isBlank()) ? Budget.OVERALL_CATEGORY : category.trim();
        Optional<Budget> existingOpt = getCategoryBudget(catTrim, year, month);
        if (existingOpt.isPresent()) {
            budgets.remove(existingOpt.get());
            storage.saveAll(budgets);
            return true;
        }
        return false;
    }

    public synchronized int updateCategoryName(String oldName, String newName) throws StorageException {
        if (oldName == null || newName == null) return 0;
        int count = 0;
        for (Budget b : budgets) {
            if (!b.isOverall() && b.getCategory().equalsIgnoreCase(oldName.trim())) {
                b.setCategory(newName.trim());
                b.setId(Budget.buildBudgetId(newName.trim(), b.getYear(), b.getMonth()));
                count++;
            }
        }
        if (count > 0) {
            storage.saveAll(budgets);
        }
        return count;
    }

    /**
     * Calculates the spending status for the overall monthly budget.
     */
    public synchronized BudgetStatus getOverallBudgetStatus(int year, int month, TransactionService txnService) {
        Optional<Budget> bgtOpt = getOverallBudget(year, month);
        BigDecimal limit = bgtOpt.map(Budget::getMonthlyLimit).orElse(BigDecimal.ZERO);

        // Calculate actual expense sum for that month
        BigDecimal totalSpent = BigDecimal.ZERO;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        for (Transaction t : txnService.getAllTransactions()) {
            if (t.isExpense() && !t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                totalSpent = totalSpent.add(t.getAmount());
            }
        }

        return new BudgetStatus(Budget.OVERALL_CATEGORY, limit, totalSpent);
    }

    /**
     * Calculates spending status for all category budgets for the specified month.
     */
    public synchronized List<BudgetStatus> getCategoryBudgetStatuses(int year, int month, TransactionService txnService) {
        List<BudgetStatus> list = new ArrayList<>();
        List<Budget> monthlyBudgets = getBudgetsForMonth(year, month);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        // Map category to spent amount
        Map<String, BigDecimal> spentMap = new HashMap<>();
        for (Transaction t : txnService.getAllTransactions()) {
            if (t.isExpense() && !t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                String cat = t.getCategory();
                spentMap.put(cat, spentMap.getOrDefault(cat, BigDecimal.ZERO).add(t.getAmount()));
            }
        }

        for (Budget b : monthlyBudgets) {
            if (b.isOverall()) continue; // Handled separately
            BigDecimal spent = spentMap.getOrDefault(b.getCategory(), BigDecimal.ZERO);
            list.add(new BudgetStatus(b.getCategory(), b.getMonthlyLimit(), spent));
        }

        return list;
    }

    /**
     * Generates a list of all active budget warnings and alerts for a given month.
     */
    public synchronized List<String> getBudgetAlertMessages(int year, int month, TransactionService txnService) {
        List<String> alerts = new ArrayList<>();
        BudgetStatus overall = getOverallBudgetStatus(year, month, txnService);

        if (overall.getLimit().compareTo(BigDecimal.ZERO) > 0) {
            if (overall.isExceeded()) {
                alerts.add("CRITICAL: Overall Monthly Budget exceeded! Spent " + overall.getSpent() + 
                           " of " + overall.getLimit() + " (" + overall.getUtilizationPercentage() + "%)");
            } else if (overall.isWarning()) {
                alerts.add("WARNING: Overall Monthly Budget is at " + overall.getUtilizationPercentage() + 
                           "% (Spent " + overall.getSpent() + " of " + overall.getLimit() + ")");
            }
        }

        for (BudgetStatus catStatus : getCategoryBudgetStatuses(year, month, txnService)) {
            if (catStatus.isExceeded()) {
                alerts.add("CRITICAL: Category '" + catStatus.getCategory() + "' exceeded budget! (" + 
                           catStatus.getUtilizationPercentage() + "%)");
            } else if (catStatus.isWarning()) {
                alerts.add("WARNING: Category '" + catStatus.getCategory() + "' reached " + 
                           catStatus.getUtilizationPercentage() + "% of budget");
            }
        }

        return alerts;
    }

    public synchronized void reload() throws StorageException {
        loadBudgets();
    }
}
