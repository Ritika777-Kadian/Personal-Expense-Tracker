package expense.service;

import expense.exception.DuplicateEntityException;
import expense.exception.EntityNotFoundException;
import expense.exception.StorageException;
import expense.exception.ValidationException;
import expense.model.Category;
import expense.model.TransactionType;
import expense.storage.CategoryStorage;
import expense.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business service managing income and expense categories.
 */
public class CategoryService {

    private final CategoryStorage storage;
    private final List<Category> categories;

    public CategoryService(CategoryStorage storage) throws StorageException {
        this.storage = storage;
        this.categories = new ArrayList<>();
        loadCategories();
    }

    private void loadCategories() throws StorageException {
        categories.clear();
        categories.addAll(storage.loadAll());
        Collections.sort(categories);
    }

    public synchronized List<Category> getAllCategories() {
        return new ArrayList<>(categories);
    }

    public synchronized List<Category> getCategoriesByType(TransactionType type) {
        return categories.stream()
                .filter(c -> c.getType() == type)
                .sorted()
                .collect(Collectors.toList());
    }

    public synchronized List<String> getCategoryNamesByType(TransactionType type) {
        return getCategoriesByType(type).stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }

    public synchronized Category addCategory(String name, TransactionType type) 
            throws ValidationException, DuplicateEntityException, StorageException {
        ValidationUtil.validateFieldNotEmpty("Category Name", name);
        if (type == null) {
            throw new ValidationException("Category Type", "Transaction type is required");
        }

        String trimmed = name.trim();
        boolean exists = categories.stream()
                .anyMatch(c -> c.getType() == type && c.getName().equalsIgnoreCase(trimmed));
        if (exists) {
            throw new DuplicateEntityException("A " + type.getDisplayName() + " category named '" + trimmed + "' already exists");
        }

        Category cat = new Category(trimmed, type, false);
        categories.add(cat);
        Collections.sort(categories);
        storage.saveAll(categories);
        return cat;
    }

    public synchronized void renameCategory(String oldName, String newName, TransactionType type, TransactionService txnService) 
            throws ValidationException, DuplicateEntityException, EntityNotFoundException, StorageException {
        renameCategory(oldName, newName, type, txnService, null);
    }

    public synchronized void renameCategory(String oldName, String newName, TransactionType type, 
                                            TransactionService txnService, BudgetService budgetService) 
            throws ValidationException, DuplicateEntityException, EntityNotFoundException, StorageException {
        ValidationUtil.validateFieldNotEmpty("New Category Name", newName);
        String trimmedOld = oldName.trim();
        String trimmedNew = newName.trim();

        Category target = categories.stream()
                .filter(c -> c.getType() == type && c.getName().equalsIgnoreCase(trimmedOld))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + trimmedOld));

        if (!trimmedOld.equalsIgnoreCase(trimmedNew)) {
            boolean exists = categories.stream()
                    .anyMatch(c -> c.getType() == type && c.getName().equalsIgnoreCase(trimmedNew));
            if (exists) {
                throw new DuplicateEntityException("A " + type.getDisplayName() + " category named '" + trimmedNew + "' already exists");
            }
        }

        target.setName(trimmedNew);
        Collections.sort(categories);
        storage.saveAll(categories);

        // Update all existing transactions with this category name
        if (txnService != null) {
            txnService.updateCategoryName(trimmedOld, trimmedNew, type);
        }

        // Update any existing budgets using this category name
        if (budgetService != null && type == TransactionType.EXPENSE) {
            budgetService.updateCategoryName(trimmedOld, trimmedNew);
        }
    }

    public synchronized boolean deleteCategory(String name, TransactionType type, TransactionService txnService) 
            throws ValidationException, EntityNotFoundException, StorageException {
        String trimmed = name.trim();
        Category target = categories.stream()
                .filter(c -> c.getType() == type && c.getName().equalsIgnoreCase(trimmed))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + trimmed));

        // Check if category is used in existing transactions
        if (txnService != null) {
            int usedCount = txnService.countTransactionsForCategory(trimmed, type);
            if (usedCount > 0) {
                throw new ValidationException("Category In Use", 
                        "Cannot delete category '" + trimmed + "': It is assigned to " + usedCount + 
                        " existing transaction(s). Please edit or reassign those transactions first.");
            }
        }

        boolean removed = categories.remove(target);
        if (removed) {
            storage.saveAll(categories);
        }
        return removed;
    }

    public synchronized void reload() throws StorageException {
        loadCategories();
    }
}
