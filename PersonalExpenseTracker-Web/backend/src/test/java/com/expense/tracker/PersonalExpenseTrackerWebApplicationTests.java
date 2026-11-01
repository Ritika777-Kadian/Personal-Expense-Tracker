package com.expense.tracker;

import com.expense.tracker.dto.*;
import com.expense.tracker.exception.ValidationException;
import com.expense.tracker.model.TransactionType;
import com.expense.tracker.model.User;
import com.expense.tracker.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PersonalExpenseTrackerWebApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private SampleDataService sampleDataService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        AuthResponse res1 = userService.register(new RegisterRequest("alice", "alice@example.com", "Password123!", "Alice Tester"));
        AuthResponse res2 = userService.register(new RegisterRequest("bob", "bob@example.com", "Password123!", "Bob Tester"));

        testUser1 = new User();
        testUser1.setId(res1.getId());
        testUser1.setUsername(res1.getUsername());

        testUser2 = new User();
        testUser2.setId(res2.getId());
        testUser2.setUsername(res2.getUsername());
    }

    @Test
    @DisplayName("Test 1: User Registration, Password Hashing, and JWT Issuance")
    void testUserRegistrationAndAuth() {
        AuthResponse loginRes = userService.login(new AuthRequest("alice", "Password123!"));
        assertNotNull(loginRes.getToken());
        assertEquals("alice", loginRes.getUsername());
        assertEquals("\u20B9", loginRes.getCurrencySymbol()); // INR Rupee symbol ₹
    }

    @Test
    @DisplayName("Test 2: Automatic Default Category Seeding on Registration")
    void testDefaultCategoriesCreated() {
        List<CategoryDTO> categories = categoryService.getCategoriesForUser(testUser1, null);
        assertFalse(categories.isEmpty());
        assertTrue(categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase("Food")));
        assertTrue(categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase("Salary")));
    }

    @Test
    @DisplayName("Test 3: Transaction Creation and Validation")
    void testCreateTransaction() {
        TransactionDTO dto = new TransactionDTO(null, TransactionType.INCOME, new BigDecimal("50000.00"), "Salary", "Monthly Salary", LocalDate.now(), false);
        TransactionDTO created = transactionService.createTransaction(testUser1, dto);
        assertNotNull(created.getId());
        assertEquals(new BigDecimal("50000.00"), created.getAmount());

        // Negative amount must be rejected
        TransactionDTO badDto = new TransactionDTO(null, TransactionType.EXPENSE, new BigDecimal("-10.00"), "Food", "Invalid", LocalDate.now(), false);
        assertThrows(ValidationException.class, () -> transactionService.createTransaction(testUser1, badDto));
    }

    @Test
    @DisplayName("Test 4: Category Deletion Safety Guard (Prevents Orphan Transactions)")
    void testCategorySafetyGuard() {
        TransactionDTO dto = new TransactionDTO(null, TransactionType.EXPENSE, new BigDecimal("500.00"), "Food", "Lunch", LocalDate.now(), false);
        transactionService.createTransaction(testUser1, dto);

        List<CategoryDTO> cats = categoryService.getCategoriesForUser(testUser1, TransactionType.EXPENSE);
        CategoryDTO foodCat = cats.stream().filter(c -> c.getName().equalsIgnoreCase("Food")).findFirst().orElseThrow();

        // Attempting to delete category used by transactions must throw ValidationException
        assertThrows(ValidationException.class, () -> categoryService.deleteCategory(testUser1, foodCat.getId()));
    }

    @Test
    @DisplayName("Test 5: Budget Status & Alert Thresholds (80% Warning and >100% Exceeded)")
    void testBudgetAlertThresholds() {
        int year = 2026;
        int month = 11;

        // Set budget of 1000.00
        budgetService.setBudget(testUser1, new BudgetDTO(null, "Food", new BigDecimal("1000.00"), year, month));

        // Spend 850.00 (85%) -> WARNING_80
        transactionService.createTransaction(testUser1, new TransactionDTO(null, TransactionType.EXPENSE, new BigDecimal("850.00"), "Food", "Grocery", LocalDate.of(year, month, 10), false));
        List<BudgetStatusDTO> statuses = budgetService.getBudgetStatuses(testUser1, year, month);
        BudgetStatusDTO foodStatus = statuses.stream().filter(s -> s.getCategory().equalsIgnoreCase("Food")).findFirst().orElseThrow();

        assertEquals("WARNING_80", foodStatus.getAlertLevel());
        assertTrue(foodStatus.isWarning());
        assertFalse(foodStatus.isExceeded());

        // Spend another 200.00 (Total 1050.00 = 105%) -> EXCEEDED
        transactionService.createTransaction(testUser1, new TransactionDTO(null, TransactionType.EXPENSE, new BigDecimal("200.00"), "Food", "Dinner", LocalDate.of(year, month, 12), false));
        statuses = budgetService.getBudgetStatuses(testUser1, year, month);
        foodStatus = statuses.stream().filter(s -> s.getCategory().equalsIgnoreCase("Food")).findFirst().orElseThrow();

        assertEquals("EXCEEDED", foodStatus.getAlertLevel());
        assertTrue(foodStatus.isExceeded());
    }

    @Test
    @DisplayName("Test 6: Multi-Tenant Data Isolation (User A Cannot Access User B Data)")
    void testMultiTenantIsolation() {
        transactionService.createTransaction(testUser1, new TransactionDTO(null, TransactionType.INCOME, new BigDecimal("10000.00"), "Salary", "Alice Secret", LocalDate.now(), false));

        List<TransactionDTO> bobTxns = transactionService.getAllTransactions(testUser2);
        assertTrue(bobTxns.isEmpty(), "Bob must not see Alice's transactions");
    }

    @Test
    @DisplayName("Test 7: Sample Data Lifecycle (Multi-Month Seed and Safe Purge Preserving Personal Records)")
    void testSampleDataLifecycle() {
        // User creates a personal transaction
        transactionService.createTransaction(testUser1, new TransactionDTO(null, TransactionType.EXPENSE, new BigDecimal("350.00"), "Food", "Alice Personal Dinner", LocalDate.now(), false));

        // Seed sample transactions across multiple months
        int seeded = sampleDataService.seedSampleData(testUser1, 2026, 11);
        assertTrue(seeded >= 20, "Should seed at least 20 transactions across multiple months");
        assertTrue(sampleDataService.hasSampleData(testUser1));

        // Attempting to re-seed without purging should return 0 (prevent duplicates)
        int duplicateSeeded = sampleDataService.seedSampleData(testUser1, 2026, 11);
        assertEquals(0, duplicateSeeded, "Duplicate seed attempt must return 0");

        // Purge sample data
        int removed = sampleDataService.purgeSampleData(testUser1);
        assertEquals(seeded, removed);

        // Verify personal transaction is still 100% intact
        List<TransactionDTO> remaining = transactionService.getAllTransactions(testUser1);
        assertEquals(1, remaining.size());
        assertEquals("Alice Personal Dinner", remaining.get(0).getDescription());
    }

    @Test
    @DisplayName("Test 8: Financial Summary & All-Time Balance Calculation")
    void testFinancialSummary() {
        int year = 2026;
        int month = 11;

        transactionService.createTransaction(testUser1, new TransactionDTO(null, TransactionType.INCOME, new BigDecimal("60000.00"), "Salary", "Salary", LocalDate.of(year, month, 1), false));
        transactionService.createTransaction(testUser1, new TransactionDTO(null, TransactionType.EXPENSE, new BigDecimal("20000.00"), "Rent", "Rent", LocalDate.of(year, month, 2), false));

        FinancialSummaryDTO summary = reportService.getFinancialSummary(testUser1, year, month);
        assertEquals(new BigDecimal("60000.00"), summary.getMonthlyIncome());
        assertEquals(new BigDecimal("20000.00"), summary.getMonthlyExpense());
        assertEquals(new BigDecimal("40000.00"), summary.getMonthlySavings());
        assertEquals(new BigDecimal("40000.00"), summary.getAllTimeBalance());
        assertEquals("Rent", summary.getHighestSpendingCategory());
    }
}
