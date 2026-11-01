package expense.service;

import expense.controller.ExpenseController;
import expense.exception.StorageException;
import expense.exception.ValidationException;
import expense.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Service providing realistic academic demo dataset management.
 * Generates at least 20 comprehensive transactions and multi-tier budgets,
 * clearly labeled as [DEMO DATA], with one-click purge and restore capabilities.
 */
public class SampleDataService {

    public SampleDataService() {}

    /**
     * Generates a curated list of 20 realistic sample transactions for the specified year and month.
     * All records are clearly prefixed with [DEMO DATA] to ensure complete transparency.
     */
    public List<Transaction> generateSampleTransactions(int year, int month) {
        List<Transaction> list = new ArrayList<>();
        YearMonth ym = YearMonth.of(year, month);
        int maxDay = ym.lengthOfMonth();

        String tag = Transaction.DEMO_TAG + " ";

        // ==================== INCOME TRANSACTIONS (4 records) ====================
        // 1. Primary Salary
        list.add(new Transaction(
                TransactionType.INCOME, new BigDecimal("65000.00"), "Salary",
                tag + "Monthly Software Engineer Stipend / Salary",
                LocalDate.of(year, month, Math.min(1, maxDay))));

        // 2. Freelance Project
        list.add(new Transaction(
                TransactionType.INCOME, new BigDecimal("16500.00"), "Freelance",
                tag + "Full-Stack Web Application for University Hackathon",
                LocalDate.of(year, month, Math.min(6, maxDay))));

        // 3. Investment Dividend
        list.add(new Transaction(
                TransactionType.INCOME, new BigDecimal("3800.00"), "Investments",
                tag + "Quarterly Mutual Fund Growth Dividend",
                LocalDate.of(year, month, Math.min(15, maxDay))));

        // 4. Honorarium / Other Income
        list.add(new Transaction(
                TransactionType.INCOME, new BigDecimal("2500.00"), "Other Income",
                tag + "Peer-to-Peer Data Structures Tutoring Honorarium",
                LocalDate.of(year, month, Math.min(22, maxDay))));

        // ==================== EXPENSE TRANSACTIONS (16 records) ====================
        // 5. Housing Rent
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("15000.00"), "Rent",
                tag + "Student Housing & Apartment Monthly Rent",
                LocalDate.of(year, month, Math.min(2, maxDay))));

        // 6. Food - Groceries
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("4850.00"), "Food",
                tag + "Supermarket Grocery Shopping & Pantry Supplies",
                LocalDate.of(year, month, Math.min(4, maxDay))));

        // 7. Bills - Internet & Electricity
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("3250.00"), "Bills",
                tag + "Electricity & High-Speed Fiber Internet Bill",
                LocalDate.of(year, month, Math.min(5, maxDay))));

        // 8. Transportation - Transit Pass
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("2600.00"), "Transportation",
                tag + "City Metro Rail Smartcard Monthly Recharge",
                LocalDate.of(year, month, Math.min(7, maxDay))));

        // 9. Education - Textbooks & Notes
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("3400.00"), "Education",
                tag + "Semester Engineering Textbooks & Reference Guides",
                LocalDate.of(year, month, Math.min(9, maxDay))));

        // 10. Food - Campus Diners
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("3600.00"), "Food",
                tag + "Campus Cafeteria Lunches & Study Group Coffee",
                LocalDate.of(year, month, Math.min(12, maxDay))));

        // 11. Entertainment - Movies & Streaming
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("1850.00"), "Entertainment",
                tag + "Weekend Cinema Tickets & OTT Video Streaming",
                LocalDate.of(year, month, Math.min(14, maxDay))));

        // 12. Shopping - Apparel & Gear
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("4200.00"), "Shopping",
                tag + "Winter Casual Apparel & Campus Laptop Backpack",
                LocalDate.of(year, month, Math.min(16, maxDay))));

        // 13. Healthcare - Medical Checkup
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("1450.00"), "Healthcare",
                tag + "Preventative Dental Checkup & Multivitamins",
                LocalDate.of(year, month, Math.min(18, maxDay))));

        // 14. Travel - Hometown Visit
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("2100.00"), "Travel",
                tag + "Intercity Express Train Ticket for Weekend Hometown Visit",
                LocalDate.of(year, month, Math.min(20, maxDay))));

        // 15. Bills - Cloud Hosting
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("1200.00"), "Bills",
                tag + "Cloud Developer VPS Hosting & Domain Renewal",
                LocalDate.of(year, month, Math.min(21, maxDay))));

        // 16. Healthcare - Fitness Membership
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("1500.00"), "Healthcare",
                tag + "University Gymnasium & Sports Complex Pass",
                LocalDate.of(year, month, Math.min(23, maxDay))));

        // 17. Bills - Mobile Plan
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("799.00"), "Bills",
                tag + "Mobile Cellular 5G Postpaid Unlimited Plan",
                LocalDate.of(year, month, Math.min(25, maxDay))));

        // 18. Food - Weekend Dinner
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("2400.00"), "Food",
                tag + "Team Project Celebration & Weekend Diners",
                LocalDate.of(year, month, Math.min(26, maxDay))));

        // 19. Education - Certification
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("2800.00"), "Education",
                tag + "Oracle Java SE Certified Associate Exam Voucher",
                LocalDate.of(year, month, Math.min(27, maxDay))));

        // 20. Shopping - Tech Accessories
        list.add(new Transaction(
                TransactionType.EXPENSE, new BigDecimal("1950.00"), "Shopping",
                tag + "Ergonomic Optical Mouse & USB-C Multiport Hub",
                LocalDate.of(year, month, Math.min(28, maxDay))));

        return list;
    }

    /**
     * Loads the 20 sample transactions and multi-tier budgets into the system and persists them to disk.
     */
    public synchronized void loadSampleData(ExpenseController controller) throws ValidationException, StorageException {
        YearMonth now = YearMonth.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // 1. Remove existing demo records first to avoid duplicates
        removeSampleData(controller, false);

        // 2. Set Up Sample Monthly and Category-wise Budgets
        // Total budget = 52,000. Total expenses will be ~53,049 -> showcases realistic threshold evaluation!
        controller.getBudgetService().setBudget(Budget.OVERALL_CATEGORY, new BigDecimal("55000.00"), year, month);

        controller.getBudgetService().setBudget("Rent", new BigDecimal("15000.00"), year, month);
        controller.getBudgetService().setBudget("Food", new BigDecimal("11000.00"), year, month); // Spent: 10,850 (98.6% -> WARNING!)
        controller.getBudgetService().setBudget("Bills", new BigDecimal("5500.00"), year, month); // Spent: 5,249
        controller.getBudgetService().setBudget("Transportation", new BigDecimal("3500.00"), year, month); // Spent: 2,600
        controller.getBudgetService().setBudget("Education", new BigDecimal("6500.00"), year, month); // Spent: 6,200
        controller.getBudgetService().setBudget("Shopping", new BigDecimal("6500.00"), year, month); // Spent: 6,150
        controller.getBudgetService().setBudget("Entertainment", new BigDecimal("2500.00"), year, month); // Spent: 1,850
        controller.getBudgetService().setBudget("Healthcare", new BigDecimal("3200.00"), year, month); // Spent: 2,950
        controller.getBudgetService().setBudget("Travel", new BigDecimal("3000.00"), year, month); // Spent: 2,100

        // 3. Add all 20 sample transactions
        List<Transaction> sampleTxns = generateSampleTransactions(year, month);
        for (Transaction t : sampleTxns) {
            controller.getTransactionService().addTransaction(t);
        }

        // 4. Update AppSettings
        AppSettings settings = controller.getSettings();
        settings.setSampleDataActive(true);
        settings.setFirstLaunchPromptShown(true);
        controller.saveSettings(settings);

        // 5. Notify all UI listeners
        controller.notifyDataChanged();
    }

    /**
     * Purges only the records marked with [DEMO DATA]. User-created transactions are completely preserved.
     */
    public synchronized int removeSampleData(ExpenseController controller, boolean notify) throws StorageException {
        List<Transaction> all = controller.getTransactionService().getAllTransactions();
        List<Transaction> toKeep = new ArrayList<>();
        int removedCount = 0;

        for (Transaction t : all) {
            if (t.isDemoData()) {
                removedCount++;
            } else {
                toKeep.add(t);
            }
        }

        if (removedCount > 0) {
            controller.getFileManager().getTransactionStorage().saveAll(toKeep);
            controller.getTransactionService().reload();
        }

        AppSettings settings = controller.getSettings();
        settings.setSampleDataActive(false);
        controller.saveSettings(settings);

        if (notify) {
            controller.notifyDataChanged();
        }

        return removedCount;
    }

    public synchronized boolean hasSampleData(ExpenseController controller) {
        for (Transaction t : controller.getTransactionService().getAllTransactions()) {
            if (t.isDemoData()) {
                return true;
            }
        }
        return false;
    }

    public synchronized int getSampleDataCount(ExpenseController controller) {
        int count = 0;
        for (Transaction t : controller.getTransactionService().getAllTransactions()) {
            if (t.isDemoData()) {
                count++;
            }
        }
        return count;
    }
}
