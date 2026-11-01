package com.expense.tracker.controller;

import com.expense.tracker.dto.CategoryDTO;
import com.expense.tracker.model.TransactionType;
import com.expense.tracker.model.User;
import com.expense.tracker.service.CategoryService;
import com.expense.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories(@RequestParam(required = false) TransactionType type) {
        User user = userService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(categoryService.getCategoriesForUser(user, type));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        User user = userService.getCurrentAuthenticatedUser();
        CategoryDTO created = categoryService.addCategory(user, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        User user = userService.getCurrentAuthenticatedUser();
        CategoryDTO updated = categoryService.updateCategory(user, id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        User user = userService.getCurrentAuthenticatedUser();
        categoryService.deleteCategory(user, id);
        return ResponseEntity.noContent().build();
    }
}
