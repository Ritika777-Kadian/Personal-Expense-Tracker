package com.expense.tracker.service;

import com.expense.tracker.dto.BudgetDTO;
import com.expense.tracker.dto.BudgetStatusDTO;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.exception.ValidationException;
import com.expense.tracker.model.Budget;
import com.expense.tracker.model.Transaction;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.BudgetRepository;
import com.expense.tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<BudgetStatusDTO> getBudgetStatuses(User user, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Transaction> monthlyTransactions = transactionRepository.findByUserIdAndDateBetweenOrderByDateDescIdDesc(
                user.getId(), start, end
        );

        List<Budget> budgets = budgetRepository.findByUserIdAndYearAndMonth(user.getId(), year, month);
        List<BudgetStatusDTO> results = new ArrayList<>();

        for (Budget b : budgets) {
            BigDecimal spent = BigDecimal.ZERO;
            if (b.isOverall()) {
                for (Transaction t : monthlyTransactions) {
                    if (t.isExpense()) {
                        spent = spent.add(t.getAmount());
                    }
                }
            } else {
                for (Transaction t : monthlyTransactions) {
                    if (t.isExpense() && t.getCategory().equalsIgnoreCase(b.getCategory())) {
                        spent = spent.add(t.getAmount());
                    }
                }
            }
            spent = spent.setScale(2, RoundingMode.HALF_UP);

            BigDecimal limit = b.getMonthlyLimit().setScale(2, RoundingMode.HALF_UP);
            BigDecimal remaining = limit.subtract(spent).setScale(2, RoundingMode.HALF_UP);

            double utilPercentage = 0.0;
            if (limit.compareTo(BigDecimal.ZERO) > 0) {
                double ratio = spent.doubleValue() / limit.doubleValue();
                utilPercentage = Math.round(ratio * 10000.0) / 100.0;
            }

            String alertLevel = "NORMAL";
            if (utilPercentage > 100.0) {
                alertLevel = "EXCEEDED";
            } else if (utilPercentage >= 80.0) {
                alertLevel = "WARNING_80";
            }

            results.add(new BudgetStatusDTO(b.getId(), b.getCategory(), limit, spent, remaining, utilPercentage, alertLevel));
        }

        return results;
    }

    @Transactional
    public BudgetDTO setBudget(User user, BudgetDTO dto) {
        if (dto.getMonthlyLimit() == null || dto.getMonthlyLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Budget limit must be greater than zero");
        }
        if (dto.getYear() == null || dto.getMonth() == null || dto.getMonth() < 1 || dto.getMonth() > 12) {
            throw new ValidationException("Valid year and month (1-12) are required");
        }

        String category = (dto.getCategory() == null || dto.getCategory().isBlank()) ? Budget.OVERALL_CATEGORY : dto.getCategory().trim();

        Optional<Budget> existing = budgetRepository.findByUserIdAndCategoryIgnoreCaseAndYearAndMonth(
                user.getId(), category, dto.getYear(), dto.getMonth()
        );

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setMonthlyLimit(dto.getMonthlyLimit().setScale(2, RoundingMode.HALF_UP));
        } else {
            budget = new Budget(category, dto.getMonthlyLimit().setScale(2, RoundingMode.HALF_UP), dto.getYear(), dto.getMonth(), user);
        }

        Budget saved = budgetRepository.save(budget);
        return new BudgetDTO(saved.getId(), saved.getCategory(), saved.getMonthlyLimit(), saved.getYear(), saved.getMonth());
    }

    @Transactional
    public void deleteBudget(User user, Long id) {
        Budget b = budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID " + id));
        budgetRepository.delete(b);
    }
}
