package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryDTO;
import com.expense.tracker.exception.DuplicateResourceException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.exception.ValidationException;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.TransactionType;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.BudgetRepository;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Transactional
    public void initializeDefaultCategories(User user) {
        List<Category> defaults = new ArrayList<>();
        // Income categories
        defaults.add(new Category("Salary", TransactionType.INCOME, true, "#10B981", "Briefcase", user));
        defaults.add(new Category("Business", TransactionType.INCOME, true, "#059669", "Building2", user));
        defaults.add(new Category("Freelance", TransactionType.INCOME, true, "#14B8A6", "Laptop", user));
        defaults.add(new Category("Investments", TransactionType.INCOME, true, "#06B6D4", "TrendingUp", user));
        defaults.add(new Category("Other Income", TransactionType.INCOME, true, "#64748B", "Coins", user));

        // Expense categories
        defaults.add(new Category("Food", TransactionType.EXPENSE, true, "#F59E0B", "Utensils", user));
        defaults.add(new Category("Shopping", TransactionType.EXPENSE, true, "#EC4899", "ShoppingBag", user));
        defaults.add(new Category("Transportation", TransactionType.EXPENSE, true, "#3B82F6", "Bus", user));
        defaults.add(new Category("Education", TransactionType.EXPENSE, true, "#8B5CF6", "GraduationCap", user));
        defaults.add(new Category("Healthcare", TransactionType.EXPENSE, true, "#EF4444", "HeartPulse", user));
        defaults.add(new Category("Entertainment", TransactionType.EXPENSE, true, "#A855F7", "Film", user));
        defaults.add(new Category("Rent", TransactionType.EXPENSE, true, "#6366F1", "Home", user));
        defaults.add(new Category("Bills", TransactionType.EXPENSE, true, "#F97316", "FileText", user));
        defaults.add(new Category("Travel", TransactionType.EXPENSE, true, "#0EA5E9", "Plane", user));
        defaults.add(new Category("Other Expenses", TransactionType.EXPENSE, true, "#94A3B8", "CreditCard", user));

        categoryRepository.saveAll(defaults);
    }

    public List<CategoryDTO> getCategoriesForUser(User user, TransactionType type) {
        List<Category> list = (type != null) ?
                categoryRepository.findByUserIdAndTypeOrderByNameAsc(user.getId(), type) :
                categoryRepository.findByUserIdOrderByNameAsc(user.getId());

        List<CategoryDTO> dtos = new ArrayList<>();
        for (Category c : list) {
            long count = transactionRepository.countByUserIdAndCategoryIgnoreCase(user.getId(), c.getName());
            dtos.add(new CategoryDTO(c.getId(), c.getName(), c.getType(), c.isDefault(), c.getColor(), c.getIcon(), count));
        }
        return dtos;
    }

    @Transactional
    public CategoryDTO addCategory(User user, CategoryDTO dto) {
        String cleanName = dto.getName().trim();
        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(user.getId(), cleanName, dto.getType())) {
            throw new DuplicateResourceException("Category '" + cleanName + "' already exists for " + dto.getType());
        }

        String color = dto.getColor() != null && !dto.getColor().isBlank() ? dto.getColor() :
                (dto.getType() == TransactionType.INCOME ? "#10B981" : "#F59E0B");
        String icon = dto.getIcon() != null && !dto.getIcon().isBlank() ? dto.getIcon() : "Tag";

        Category category = new Category(cleanName, dto.getType(), false, color, icon, user);
        Category saved = categoryRepository.save(category);
        return new CategoryDTO(saved.getId(), saved.getName(), saved.getType(), saved.isDefault(), saved.getColor(), saved.getIcon(), 0);
    }

    @Transactional
    public CategoryDTO updateCategory(User user, Long categoryId, CategoryDTO dto) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String oldName = category.getName();
        String newName = dto.getName().trim();

        if (!oldName.equalsIgnoreCase(newName)) {
            if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(user.getId(), newName, category.getType())) {
                throw new DuplicateResourceException("Another category named '" + newName + "' already exists");
            }
            category.setName(newName);
            // Cascade rename to transactions and budgets
            transactionRepository.updateCategoryName(user.getId(), oldName, newName);
            budgetRepository.updateCategoryName(user.getId(), oldName, newName);
        }

        if (dto.getColor() != null && !dto.getColor().isBlank()) {
            category.setColor(dto.getColor());
        }
        if (dto.getIcon() != null && !dto.getIcon().isBlank()) {
            category.setIcon(dto.getIcon());
        }

        Category saved = categoryRepository.save(category);
        long count = transactionRepository.countByUserIdAndCategoryIgnoreCase(user.getId(), saved.getName());
        return new CategoryDTO(saved.getId(), saved.getName(), saved.getType(), saved.isDefault(), saved.getColor(), saved.getIcon(), count);
    }

    @Transactional
    public void deleteCategory(User user, Long categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        long usageCount = transactionRepository.countByUserIdAndCategoryIgnoreCase(user.getId(), category.getName());
        if (usageCount > 0) {
            throw new ValidationException("Cannot delete category '" + category.getName() + "' because it is linked to " +
                    usageCount + " existing transaction(s). Reassign or remove these transactions first.");
        }

        categoryRepository.delete(category);
    }
}
