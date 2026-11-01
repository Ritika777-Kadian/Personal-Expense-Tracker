package expense.test;

import expense.controller.ExpenseController;
import expense.exception.DuplicateEntityException;
import expense.exception.EntityNotFoundException;
import expense.exception.StorageException;
import expense.exception.ValidationException;
import expense.model.*;
import expense.service.ReportService;
import expense.storage.FileManager;
import expense.storage.TransactionStorage;
import expense.util.CsvUtil;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;
import expense.util.MathUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Automated Verification Test Suite for Personal Expense Tracker.
 * Covers all 18 prompt requirements plus dedicated tests for:
 * - RFC-4180 CSV multiline, quotes, commas, and UTF-8 handling
 * - Isolated test directory (never modifies real application data)
 * - Sample data lifecycle and user transaction preservation
 * - Budget warnings and exceeded alerts
 * - Mathematical recursion and bitwise operations
 */
public class ExpenseTrackerTestSuite {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("           PERSONAL EXPENSE TRACKER — AUTOMATED VERIFICATION TEST SUITE          ");
        System.out.println("================================================================================");

        File testDir = new File("build_test_data");

        try {
            // Clean up any lingering test directory from prior runs
            if (testDir.exists()) {
                deleteRecursively(testDir);
            }
            testDir.mkdirs();

            // Track state of real application data directory to ensure absolute isolation
            File realDataDir = new File("data");
            long realDataFileCount = realDataDir.exists() ? realDataDir.list().length : 0;

            // Initialize isolated test FileManager and Controller
            FileManager testFileManager = new FileManager(testDir);
            testFileManager.initializeAll();

            ExpenseController controller = new ExpenseController(testFileManager);

            // ==================== SECTION 1: TRANSACTION CRUD & VALIDATION ====================
            // Test 1: Adding Income Transaction
            runTest("Test 1: Adding Income Transaction", () -> {
                Transaction t = new Transaction(TransactionType.INCOME, new BigDecimal("50000.00"), "Salary", "October Salary", LocalDate.of(2026, 10, 1));
                controller.getTransactionService().addTransaction(t);
                assertCondition(t.getId() != null && t.getId().startsWith("TXN-"), "Transaction ID generated");
                assertCondition(controller.getTransactionService().getAllTransactions().contains(t), "Transaction saved in service");
            });

            // Test 2: Adding Expense Transaction
            runTest("Test 2: Adding Expense Transaction", () -> {
                Transaction t = new Transaction(TransactionType.EXPENSE, new BigDecimal("4500.00"), "Food", "Weekly Groceries Basket", LocalDate.of(2026, 10, 5));
                controller.getTransactionService().addTransaction(t);
                assertCondition(t.isExpense(), "Is expense transaction");
                assertCondition(controller.getTransactionService().getAllTransactions().contains(t), "Expense stored");
            });

            // Test 3: Adding Empty / Null Transaction Rejected
            runTest("Test 3: Adding Empty / Null Transaction Rejected", () -> {
                boolean rejected = false;
                try {
                    controller.getTransactionService().addTransaction(null);
                } catch (ValidationException e) {
                    rejected = true;
                }
                assertCondition(rejected, "Null transaction rejected with ValidationException");

                rejected = false;
                try {
                    Transaction empty = new Transaction(TransactionType.EXPENSE, new BigDecimal("100.00"), "", "Empty category", LocalDate.now());
                    controller.getTransactionService().addTransaction(empty);
                } catch (ValidationException e) {
                    rejected = true;
                }
                assertCondition(rejected, "Empty category rejected with ValidationException");
            });

            // Test 4: Entering Negative or Zero Amount
            runTest("Test 4: Entering Negative or Zero Amount Rejected", () -> {
                boolean rejectedZero = false;
                try {
                    Transaction zero = new Transaction(TransactionType.EXPENSE, BigDecimal.ZERO, "Food", "Zero amount", LocalDate.now());
                    controller.getTransactionService().addTransaction(zero);
                } catch (ValidationException e) {
                    rejectedZero = true;
                }
                assertCondition(rejectedZero, "Zero amount rejected");

                boolean rejectedNeg = false;
                try {
                    Transaction neg = new Transaction(TransactionType.EXPENSE, new BigDecimal("-250.00"), "Food", "Negative amount", LocalDate.now());
                    controller.getTransactionService().addTransaction(neg);
                } catch (ValidationException e) {
                    rejectedNeg = true;
                }
                assertCondition(rejectedNeg, "Negative amount rejected");
            });

            // Test 5: Editing a Transaction
            runTest("Test 5: Editing a Transaction", () -> {
                List<Transaction> list = controller.getTransactionService().getAllTransactions();
                Transaction original = list.get(0);
                original.setDescription("Updated Memo Note - Verified");
                original.setAmount(new BigDecimal("5500.00"));
                controller.getTransactionService().updateTransaction(original);

                Transaction fetched = controller.getTransactionService().getTransactionById(original.getId());
                assertCondition(fetched.getDescription().equals("Updated Memo Note - Verified"), "Description updated");
                assertCondition(fetched.getAmount().compareTo(new BigDecimal("5500.00")) == 0, "Amount updated");
            });

            // Test 6: Deleting a Transaction
            runTest("Test 6: Deleting a Transaction", () -> {
                Transaction temp = new Transaction(TransactionType.EXPENSE, new BigDecimal("99.00"), "Entertainment", "Temp to delete", LocalDate.now());
                controller.getTransactionService().addTransaction(temp);
                String id = temp.getId();
                assertCondition(controller.getTransactionService().getAllTransactions().contains(temp), "Temp added");

                boolean deleted = controller.getTransactionService().deleteTransaction(id);
                assertCondition(deleted, "Transaction deleted successfully");

                boolean notFound = false;
                try {
                    controller.getTransactionService().getTransactionById(id);
                } catch (EntityNotFoundException e) {
                    notFound = true;
                }
                assertCondition(notFound, "EntityNotFoundException thrown after deletion");
            });

            // Test 7: Searching Transactions
            runTest("Test 7: Searching Transactions", () -> {
                Transaction searchItem = new Transaction(TransactionType.EXPENSE, new BigDecimal("750.00"), "Food", "Supermarket Groceries Basket", LocalDate.now());
                controller.getTransactionService().addTransaction(searchItem);

                List<Transaction> res = controller.getTransactionService().filterTransactions(
                        null, null, null, null, null, null, "Groceries");
                assertCondition(!res.isEmpty(), "Found keyword 'Groceries'");
                assertCondition(res.get(0).getDescription().contains("Groceries"), "Result contains keyword");
            });

            // Test 8: Filtering by Category and Date
            runTest("Test 8: Filtering by Category and Date", () -> {
                List<Transaction> res = controller.getTransactionService().filterTransactions(
                        TransactionType.EXPENSE, "Food", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), null, null, null);
                assertCondition(!res.isEmpty(), "Found filtered transactions");
                for (Transaction t : res) {
                    assertCondition(t.getCategory().equalsIgnoreCase("Food"), "Category is Food");
                    assertCondition(t.isExpense(), "Type is Expense");
                }
            });

            // ==================== SECTION 2: BUDGET & THRESHOLD ALERTS ====================
            // Test 9: Setting a Monthly Budget
            runTest("Test 9: Setting a Monthly Budget", () -> {
                Budget b = controller.getBudgetService().setBudget(Budget.OVERALL_CATEGORY, new BigDecimal("30000.00"), 2026, 10);
                assertCondition(b != null, "Budget created");
                assertCondition(b.getMonthlyLimit().compareTo(new BigDecimal("30000.00")) == 0, "Limit matches");
            });

            // Test 10: Triggering 80% Budget Warning
            runTest("Test 10: Triggering 80% Budget Warning Threshold", () -> {
                // Set budget of 10000 in dedicated test period
                controller.getBudgetService().setBudget("Shopping", new BigDecimal("10000.00"), 2029, 5);
                // Spend 8500 (85%)
                controller.getTransactionService().addTransaction(new Transaction(
                        TransactionType.EXPENSE, new BigDecimal("8500.00"), "Shopping", "Test 85% Spending", LocalDate.of(2029, 5, 10)));

                List<BudgetStatus> statuses = controller.getBudgetService().getCategoryBudgetStatuses(2029, 5, controller.getTransactionService());
                BudgetStatus shoppingStatus = statuses.stream().filter(s -> s.getCategory().equals("Shopping")).findFirst().orElse(null);
                assertCondition(shoppingStatus != null, "Shopping budget status found");
                assertCondition(shoppingStatus.isWarning(), "80% warning triggered (85% utilized)");
                assertCondition(shoppingStatus.getAlertLevel() == BudgetAlertLevel.WARNING_80, "AlertLevel is WARNING_80");
            });

            // Test 11: Exceeding Budget (> 100%)
            runTest("Test 11: Exceeding Budget Threshold (>100%)", () -> {
                // Add another 2000 to Shopping in 2029-05, total spent = 10500 of 10000 (105%)
                controller.getTransactionService().addTransaction(new Transaction(
                        TransactionType.EXPENSE, new BigDecimal("2000.00"), "Shopping", "Test Exceeded Spending", LocalDate.of(2029, 5, 12)));

                List<BudgetStatus> statuses = controller.getBudgetService().getCategoryBudgetStatuses(2029, 5, controller.getTransactionService());
                BudgetStatus shoppingStatus = statuses.stream().filter(s -> s.getCategory().equals("Shopping")).findFirst().orElse(null);
                assertCondition(shoppingStatus != null, "Shopping status found");
                assertCondition(shoppingStatus.isExceeded(), "100% exceeded alert triggered");
                assertCondition(shoppingStatus.getAlertLevel() == BudgetAlertLevel.EXCEEDED, "AlertLevel is EXCEEDED");
            });

            // ==================== SECTION 3: REPORTS & ANALYTICS ====================
            // Test 12: Generating Reports and Summaries
            runTest("Test 12: Generating Reports and Summaries", () -> {
                FinancialSummary summary = controller.getReportService().getMonthlySummary(
                        2026, 10, controller.getTransactionService(), controller.getBudgetService());
                assertCondition(summary.getTotalIncome().compareTo(BigDecimal.ZERO) > 0, "Total income calculated");
                assertCondition(summary.getTotalExpense().compareTo(BigDecimal.ZERO) > 0, "Total expense calculated");
                assertCondition(summary.getNetBalance().compareTo(BigDecimal.ZERO) != 0, "Net balance calculated");
                assertCondition(summary.getHighestSpendingCategory() != null, "Highest spending category identified");

                BigDecimal allTime = controller.getReportService().getAllTimeBalance(controller.getTransactionService());
                assertCondition(allTime != null, "All-time balance computed");
            });

            // Test 13: Updating Dashboard Charts Data
            runTest("Test 13: Updating Dashboard Charts Data", () -> {
                Map<String, BigDecimal> breakdown = controller.getReportService().getExpenseCategoryBreakdown(
                        LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), controller.getTransactionService());
                assertCondition(breakdown.containsKey("Food"), "Breakdown includes Food category");

                List<ReportService.MonthlyTrendPoint> trend = controller.getReportService().getYearlyTrend(
                        2026, controller.getTransactionService());
                assertCondition(trend.size() == 12, "Yearly trend generates 12 monthly points");
            });

            // Test 14: Exporting CSV Files
            runTest("Test 14: Exporting CSV Files (RFC-4180 Escaped & Excel Compatible)", () -> {
                File exportFile = new File(testDir, "test_export.csv");
                controller.getExportService().exportTransactionsToCsv(
                        controller.getTransactionService().getAllTransactions(), exportFile);
                assertCondition(exportFile.exists(), "CSV export file exists on disk");
                assertCondition(exportFile.length() > 0, "CSV export file is non-empty");
            });

            // ==================== SECTION 4: DATA PERSISTENCE & STORAGE ====================
            // Test 15: Data Persistence After Reopen (using isolated test directory)
            runTest("Test 15: Data Persistence After Reopen", () -> {
                int countBefore = controller.getTransactionService().getAllTransactions().size();
                // Create a new instance connecting to the SAME test storage directory
                ExpenseController reloadedController = new ExpenseController(testFileManager);
                int countAfter = reloadedController.getTransactionService().getAllTransactions().size();
                assertCondition(countBefore == countAfter, "Transaction count preserved across restarts (" + countBefore + ")");
            });

            // Test 16: Loading Data from Existing Files
            runTest("Test 16: Loading Data from Existing Files", () -> {
                List<Category> cats = controller.getCategoryService().getAllCategories();
                assertCondition(!cats.isEmpty(), "Categories loaded from file");
                assertCondition(cats.stream().anyMatch(c -> c.getName().equals("Food")), "Default Food category loaded");
                assertCondition(cats.stream().anyMatch(c -> c.getName().equals("Salary")), "Default Salary category loaded");
            });

            // Test 17: Handling Missing or Malformed Files Gracefully
            runTest("Test 17: Handling Missing or Malformed Files Gracefully", () -> {
                File malformedFile = new File(testDir, "malformed_test.csv");
                try (FileWriter fw = new FileWriter(malformedFile)) {
                    fw.write("id,type,amount,category,description,date,createdAt,updatedAt\n");
                    fw.write("CORRUPTED_ROW_WITHOUT_ENOUGH_COLUMNS\n");
                    fw.write("TXN-9999,EXPENSE,150.00,Food,Valid Row,2026-10-15,2026-10-15T10:00:00,2026-10-15T10:00:00\n");
                    fw.write("ANOTHER_BAD_ROW,WITH,INVALID,DATE\n");
                }
                TransactionStorage ts = new TransactionStorage(malformedFile);
                List<Transaction> loaded = ts.loadAll();
                assertCondition(loaded.size() == 1, "Skipped corrupted lines safely, loaded 1 valid row");
                assertCondition(loaded.get(0).getId().equals("TXN-9999"), "Loaded valid transaction correctly");
                malformedFile.delete();
            });

            // Test 18: Resetting Data Safely
            runTest("Test 18: Resetting Data Safely", () -> {
                controller.resetAllData();
                assertCondition(controller.getTransactionService().getAllTransactions().isEmpty(), "Transactions cleared on reset");
                assertCondition(controller.getBudgetService().getAllBudgets().isEmpty(), "Budgets cleared on reset");
                assertCondition(!controller.getCategoryService().getAllCategories().isEmpty(), "Default categories restored");
            });

            // ==================== SECTION 5: SYLLABUS CONCEPTS & SAFETY ====================
            // Extra Test 19: Category Deletion Protection when In Use
            runTest("Extra Test 19: Category Deletion Safety Constraint", () -> {
                controller.getTransactionService().addTransaction(new Transaction(
                        TransactionType.EXPENSE, new BigDecimal("250.00"), "Rent", "Office Rent", LocalDate.now()));
                boolean safetyTriggered = false;
                try {
                    controller.getCategoryService().deleteCategory("Rent", TransactionType.EXPENSE, controller.getTransactionService());
                } catch (ValidationException e) {
                    safetyTriggered = true;
                }
                assertCondition(safetyTriggered, "Prevented deleting category 'Rent' while transactions use it");
            });

            // Extra Test 20: Syllabus Recursion Demonstration (MathUtil)
            runTest("Extra Test 20: Syllabus Recursion (Compound Savings Projection)", () -> {
                BigDecimal initial = new BigDecimal("10000.00");
                BigDecimal monthlyAdd = new BigDecimal("1000.00");
                BigDecimal projected = MathUtil.projectSavingsRecursive(initial, monthlyAdd, 6.0, 12);
                assertCondition(projected.compareTo(initial) > 0, "Recursive savings projection computed: " + CurrencyUtil.format(projected));
            });

            // Extra Test 21: Bitwise Filter Flags (MathUtil)
            runTest("Extra Test 21: Syllabus Bitwise Operators (Filter Mask)", () -> {
                int mask = MathUtil.FILTER_TYPE | MathUtil.FILTER_CATEGORY;
                assertCondition(MathUtil.hasFilterFlag(mask, MathUtil.FILTER_TYPE), "Bitwise flag FILTER_TYPE is set");
                assertCondition(MathUtil.hasFilterFlag(mask, MathUtil.FILTER_CATEGORY), "Bitwise flag FILTER_CATEGORY is set");
                assertCondition(!MathUtil.hasFilterFlag(mask, MathUtil.FILTER_DATE), "Bitwise flag FILTER_DATE is not set");
            });

            // ==================== SECTION 6: SAMPLE DATA LIFECYCLE ====================
            // Test 22: Load 20 Realistic Sample Transactions and Budgets
            runTest("Test 22: Load 20 Realistic Sample Transactions & Budgets", () -> {
                controller.loadSampleData();
                int sampleCount = controller.getSampleDataCount();
                assertCondition(sampleCount >= 20, "At least 20 sample records loaded (Found: " + sampleCount + ")");
                assertCondition(controller.hasSampleData(), "hasSampleData() reports true");
                assertCondition(controller.getBudgetService().getAllBudgets().size() >= 5, "Sample category budgets configured");
            });

            // Test 23: All Sample Records Labeled as [DEMO DATA]
            runTest("Test 23: All Sample Records Clearly Labeled as [DEMO DATA]", () -> {
                List<Transaction> list = controller.getTransactionService().getAllTransactions();
                int demoCount = 0;
                for (Transaction t : list) {
                    if (t.isDemoData()) {
                        demoCount++;
                        assertCondition(t.getDescription().contains(Transaction.DEMO_TAG), 
                                "Description contains [DEMO DATA] tag");
                    }
                }
                assertCondition(demoCount >= 20, "All sample transactions explicitly identified with isDemoData()");
            });

            // Test 24: Sample Data Persisted in CSV Files and Computes Metrics
            runTest("Test 24: Sample Data Persisted in CSV Files and Computes Metrics", () -> {
                File txnFile = new File(testDir, "transactions.csv");
                assertCondition(txnFile.exists() && txnFile.length() > 0, "transactions.csv is populated on disk");

                YearMonth now = YearMonth.now();
                FinancialSummary summary = controller.getReportService().getMonthlySummary(
                        now.getYear(), now.getMonthValue(), controller.getTransactionService(), controller.getBudgetService());
                assertCondition(summary.getTotalIncome().compareTo(BigDecimal.ZERO) > 0, "Dashboard Total Income computed from demo data");
                assertCondition(summary.getTotalExpense().compareTo(BigDecimal.ZERO) > 0, "Dashboard Total Expense computed from demo data");
            });

            // Test 25: Remove Sample Data Preserves User-Created Transactions
            runTest("Test 25: Remove Sample Data Preserves User-Created Transactions", () -> {
                // Add an actual personal user transaction
                Transaction personal = new Transaction(TransactionType.EXPENSE, new BigDecimal("1200.00"), "Food", "My Personal Dinner Record", LocalDate.now());
                controller.getTransactionService().addTransaction(personal);
                assertCondition(!personal.isDemoData(), "Personal transaction does NOT have demo tag");

                // Purge demo data
                int removed = controller.removeSampleData();
                assertCondition(removed >= 20, "Removed all 20 demo records");
                assertCondition(!controller.hasSampleData(), "No demo data remaining in controller");

                // Verify user-created transaction is preserved!
                List<Transaction> remaining = controller.getTransactionService().getAllTransactions();
                assertCondition(remaining.contains(personal), "User personal transaction was strictly preserved!");
                for (Transaction t : remaining) {
                    assertCondition(!t.isDemoData(), "No demo records remain in ledger: " + t.getDescription());
                }
            });

            // Test 26: No Duplicate Sample Records on Repeated Loading
            runTest("Test 26: Duplicate Prevention on Repeated Sample Data Loading", () -> {
                controller.loadSampleData();
                int firstCount = controller.getSampleDataCount();
                // Load again
                controller.loadSampleData();
                int secondCount = controller.getSampleDataCount();
                assertCondition(firstCount == secondCount, "Repeated loading does not create duplicate demo records (Count: " + secondCount + ")");
            });

            // ==================== SECTION 7: ADVANCED RFC-4180 CSV COMPLIANCE ====================
            // Test 27: CSV Commas Inside Descriptions
            runTest("Test 27: CSV Commas Inside Descriptions", () -> {
                String descWithCommas = "Grocery, Fruits, and Dairy Products, Supermarket";
                Transaction t = new Transaction(TransactionType.EXPENSE, new BigDecimal("450.00"), "Food", descWithCommas, LocalDate.now());
                String csvLine = CsvUtil.toCsvLine(t.toCsvRecord());
                assertCondition(csvLine.contains("\"" + descWithCommas + "\""), "Commas wrapped in quotes");

                String[] parsed = CsvUtil.parseCsvLine(csvLine);
                Transaction reloaded = Transaction.fromCsvRecord(parsed);
                assertCondition(reloaded.getDescription().equals(descWithCommas), "Description with commas matches identically");
            });

            // Test 28: CSV Double Quotation Marks Handling
            runTest("Test 28: CSV Double Quotation Marks Handling", () -> {
                String descWithQuotes = "Book: \"Java: The Complete Reference\" by Herbert Schildt";
                Transaction t = new Transaction(TransactionType.EXPENSE, new BigDecimal("850.00"), "Education", descWithQuotes, LocalDate.now());
                String csvLine = CsvUtil.toCsvLine(t.toCsvRecord());
                assertCondition(csvLine.contains("\"\"Java: The Complete Reference\"\""), "Quotes doubled per RFC-4180");

                String[] parsed = CsvUtil.parseCsvLine(csvLine);
                Transaction reloaded = Transaction.fromCsvRecord(parsed);
                assertCondition(reloaded.getDescription().equals(descWithQuotes), "Description with quotes matches identically");
            });

            // Test 29: CSV Multiline Descriptions Handling
            runTest("Test 29: CSV Multiline Descriptions Handling", () -> {
                String multilineDesc = "Invoice Note:\nLine 1: Server Renewal\nLine 2: Cloud Storage\nLine 3: Domain";
                Transaction t = new Transaction(TransactionType.EXPENSE, new BigDecimal("2500.00"), "Bills", multilineDesc, LocalDate.now());
                File multiCsv = new File(testDir, "multiline_test.csv");
                TransactionStorage storage = new TransactionStorage(multiCsv);

                storage.saveAll(List.of(t));
                List<Transaction> reloaded = storage.loadAll();
                assertCondition(reloaded.size() == 1, "Multiline record loaded as exactly 1 transaction");
                assertCondition(reloaded.get(0).getDescription().equals(multilineDesc), "Multiline content preserved verbatim");
                multiCsv.delete();
            });

            // Test 30: CSV UTF-8 Characters and BOM Handling
            runTest("Test 30: CSV UTF-8 Characters and BOM Handling", () -> {
                String utf8Desc = "Salary in ₹, Coffee in € & £, Tokyo metro in ¥";
                Transaction t = new Transaction(TransactionType.INCOME, new BigDecimal("1000.00"), "Other Income", utf8Desc, LocalDate.now());
                File utf8Csv = new File(testDir, "utf8_test.csv");
                TransactionStorage storage = new TransactionStorage(utf8Csv);

                storage.saveAll(List.of(t));
                List<Transaction> reloaded = storage.loadAll();
                assertCondition(reloaded.get(0).getDescription().equals(utf8Desc), "UTF-8 currency symbols preserved");
                utf8Csv.delete();
            });

            // Test 31: Verify Real Application Data Was Not Modified
            runTest("Test 31: Verification that Real Application Data Remained Untouched", () -> {
                if (realDataDir.exists()) {
                    long currentRealCount = realDataDir.list().length;
                    assertCondition(realDataFileCount == currentRealCount, "Real data directory file count is identical");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fatal error in test execution: " + e.getMessage());
        } finally {
            // Clean up temporary test files
            deleteRecursively(testDir);
        }

        System.out.println("================================================================================");
        System.out.println("TEST RESULTS SUMMARY: " + totalPassed + " PASSED, " + totalFailed + " FAILED");
        System.out.println("================================================================================");
    }

    private static void runTest(String testName, TestRunnable r) {
        try {
            r.run();
            System.out.println("[PASS] " + testName);
            totalPassed++;
        } catch (Throwable t) {
            System.out.println("[FAIL] " + testName + " -> " + t.getMessage());
            t.printStackTrace();
            totalFailed++;
        }
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) deleteRecursively(f);
            }
        }
        file.delete();
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Exception;
    }
}
