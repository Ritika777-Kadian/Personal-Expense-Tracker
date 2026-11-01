package com.expense.tracker.controller;

import com.expense.tracker.dto.CategoryExpenseDTO;
import com.expense.tracker.dto.FinancialSummaryDTO;
import com.expense.tracker.dto.MonthlyTrendDTO;
import com.expense.tracker.dto.TransactionDTO;
import com.expense.tracker.model.User;
import com.expense.tracker.service.ReportService;
import com.expense.tracker.service.TransactionService;
import com.expense.tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        User user = userService.getCurrentAuthenticatedUser();
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        YearMonth ym = YearMonth.of(targetYear, targetMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        FinancialSummaryDTO summary = reportService.getFinancialSummary(user, targetYear, targetMonth);
        List<CategoryExpenseDTO> categories = reportService.getCategoryBreakdown(user, start, end);
        List<MonthlyTrendDTO> trends = reportService.get12MonthTrends(user, targetYear);
        List<TransactionDTO> recentTransactions = transactionService.getAllTransactions(user)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("summary", summary);
        data.put("categoryBreakdown", categories);
        data.put("monthlyTrends", trends);
        data.put("recentTransactions", recentTransactions);
        data.put("year", targetYear);
        data.put("month", targetMonth);

        return ResponseEntity.ok(data);
    }
}
