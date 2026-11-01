package com.expense.tracker.controller;

import com.expense.tracker.dto.BudgetDTO;
import com.expense.tracker.dto.BudgetStatusDTO;
import com.expense.tracker.model.User;
import com.expense.tracker.service.BudgetService;
import com.expense.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private UserService userService;

    @Autowired
    private BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetStatusDTO>> getBudgets(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        User user = userService.getCurrentAuthenticatedUser();
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        return ResponseEntity.ok(budgetService.getBudgetStatuses(user, targetYear, targetMonth));
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> setBudget(@Valid @RequestBody BudgetDTO dto) {
        User user = userService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(budgetService.setBudget(user, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        User user = userService.getCurrentAuthenticatedUser();
        budgetService.deleteBudget(user, id);
        return ResponseEntity.noContent().build();
    }
}
