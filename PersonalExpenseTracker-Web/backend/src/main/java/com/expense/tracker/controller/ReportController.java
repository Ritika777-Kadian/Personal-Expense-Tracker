package com.expense.tracker.controller;

import com.expense.tracker.dto.CategoryExpenseDTO;
import com.expense.tracker.dto.FinancialSummaryDTO;
import com.expense.tracker.dto.MonthlyTrendDTO;
import com.expense.tracker.model.User;
import com.expense.tracker.service.ReportService;
import com.expense.tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryDTO> getSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        User user = userService.getCurrentAuthenticatedUser();
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        return ResponseEntity.ok(reportService.getFinancialSummary(user, targetYear, targetMonth));
    }

    @GetMapping("/trends")
    public ResponseEntity<List<MonthlyTrendDTO>> getTrends(@RequestParam(required = false) Integer year) {
        User user = userService.getCurrentAuthenticatedUser();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(reportService.get12MonthTrends(user, targetYear));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryExpenseDTO>> getCategoryBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        User user = userService.getCurrentAuthenticatedUser();
        YearMonth now = YearMonth.now();
        LocalDate startDate = (start != null) ? start : now.atDay(1);
        LocalDate endDate = (end != null) ? end : now.atEndOfMonth();

        return ResponseEntity.ok(reportService.getCategoryBreakdown(user, startDate, endDate));
    }
}
