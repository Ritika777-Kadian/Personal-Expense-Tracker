package com.expense.tracker.service;

import com.expense.tracker.model.*;
import com.expense.tracker.repository.BudgetRepository;
import com.expense.tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class SampleDataService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    public boolean hasSampleData(User user) {
        return !transactionRepository.findByUserIdAndIsDemoTrue(user.getId()).isEmpty();
    }

    public long getSampleDataCount(User user) {
        return transactionRepository.findByUserIdAndIsDemoTrue(user.getId()).size();
    }

    @Transactional
    public int seedSampleData(User user, int year, int month) {
        // Prevent duplicate demo data if clicked multiple times
        if (hasSampleData(user)) {
            return 0;
        }

        YearMonth targetYm = YearMonth.of(year, month);
        YearMonth ym2Ago = targetYm.minusMonths(2);
        YearMonth ym1Ago = targetYm.minusMonths(1);

        List<Transaction> list = new ArrayList<>();

        // Generate transactions across 3 consecutive months for rich charts and reports
        list.addAll(generateMonthTransactions(user, ym2Ago.getYear(), ym2Ago.getMonthValue(), 1));
        list.addAll(generateMonthTransactions(user, ym1Ago.getYear(), ym1Ago.getMonthValue(), 2));
        list.addAll(generateMonthTransactions(user, targetYm.getYear(), targetYm.getMonthValue(), 3));

        transactionRepository.saveAll(list);

        // Seed realistic tiered budgets across all 3 months
        seedBudgetsForMonth(user, ym2Ago.getYear(), ym2Ago.getMonthValue());
        seedBudgetsForMonth(user, ym1Ago.getYear(), ym1Ago.getMonthValue());
        seedBudgetsForMonth(user, targetYm.getYear(), targetYm.getMonthValue());

        return list.size();
    }

    private void seedBudgetsForMonth(User user, int y, int m) {
        List<Budget> budgets = new ArrayList<>();
        budgets.add(new Budget("Overall", new BigDecimal("55000.00"), y, m, user));
        budgets.add(new Budget("Rent", new BigDecimal("15000.00"), y, m, user));
        budgets.add(new Budget("Food", new BigDecimal("11000.00"), y, m, user));
        budgets.add(new Budget("Bills", new BigDecimal("5500.00"), y, m, user));
        budgets.add(new Budget("Transportation", new BigDecimal("3500.00"), y, m, user));
        budgets.add(new Budget("Education", new BigDecimal("6500.00"), y, m, user));
        budgets.add(new Budget("Shopping", new BigDecimal("6500.00"), y, m, user));
        budgets.add(new Budget("Healthcare", new BigDecimal("3200.00"), y, m, user));
        budgets.add(new Budget("Entertainment", new BigDecimal("2500.00"), y, m, user));
        budgets.add(new Budget("Travel", new BigDecimal("3000.00"), y, m, user));

        for (Budget b : budgets) {
            if (budgetRepository.findByUserIdAndCategoryIgnoreCaseAndYearAndMonth(user.getId(), b.getCategory(), y, m).isEmpty()) {
                budgetRepository.save(b);
            }
        }
    }

    private List<Transaction> generateMonthTransactions(User user, int year, int month, int monthIndex) {
        List<Transaction> list = new ArrayList<>();
        YearMonth ym = YearMonth.of(year, month);
        int maxDay = ym.lengthOfMonth();
        String tag = Transaction.DEMO_TAG + " ";

        if (monthIndex == 1) {
            // Month 1 (2 months prior)
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("65000.00"), "Salary",
                    tag + "Monthly Software Engineer Stipend / Salary", LocalDate.of(year, month, Math.min(1, maxDay)), true, user));
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("14000.00"), "Freelance",
                    tag + "UI/UX Design System for Startup Client", LocalDate.of(year, month, Math.min(6, maxDay)), true, user));
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("3200.00"), "Investments",
                    tag + "Stock Portfolio Dividend Payout", LocalDate.of(year, month, Math.min(15, maxDay)), true, user));
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("2000.00"), "Other Income",
                    tag + "Tech Workshop Mentorship Stipend", LocalDate.of(year, month, Math.min(22, maxDay)), true, user));

            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("15000.00"), "Rent",
                    tag + "Student Housing & Apartment Monthly Rent", LocalDate.of(year, month, Math.min(2, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("4200.00"), "Food",
                    tag + "Supermarket Groceries & Pantry Restock", LocalDate.of(year, month, Math.min(4, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("3100.00"), "Bills",
                    tag + "High-Speed Fiber Internet & Electricity Bill", LocalDate.of(year, month, Math.min(5, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2400.00"), "Transportation",
                    tag + "City Metro Card Monthly Pass", LocalDate.of(year, month, Math.min(7, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2500.00"), "Education",
                    tag + "Online Full-Stack Certification Course", LocalDate.of(year, month, Math.min(9, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2400.00"), "Food",
                    tag + "University Cafeteria & Study Snacks", LocalDate.of(year, month, Math.min(12, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1600.00"), "Entertainment",
                    tag + "IMAX Movie Tickets & Streaming Subscriptions", LocalDate.of(year, month, Math.min(14, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("3500.00"), "Shopping",
                    tag + "Winter Jackets & Casual Footwear", LocalDate.of(year, month, Math.min(16, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1200.00"), "Healthcare",
                    tag + "Routine Medical Checkup & Pharmacy", LocalDate.of(year, month, Math.min(18, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1800.00"), "Travel",
                    tag + "Weekend Train Trip to Cultural Festival", LocalDate.of(year, month, Math.min(20, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1500.00"), "Healthcare",
                    tag + "Campus Fitness & Gym Membership", LocalDate.of(year, month, Math.min(23, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("799.00"), "Bills",
                    tag + "Mobile 5G Postpaid Unlimited Plan", LocalDate.of(year, month, Math.min(25, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1850.00"), "Food",
                    tag + "Weekend Dinner with Friends", LocalDate.of(year, month, Math.min(26, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1900.00"), "Education",
                    tag + "Algorithms Reference Textbooks", LocalDate.of(year, month, Math.min(27, maxDay)), true, user));
        } else if (monthIndex == 2) {
            // Month 2 (1 month prior)
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("65000.00"), "Salary",
                    tag + "Monthly Software Engineer Stipend / Salary", LocalDate.of(year, month, Math.min(1, maxDay)), true, user));
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("18500.00"), "Freelance",
                    tag + "Full-Stack Mobile API Integration", LocalDate.of(year, month, Math.min(6, maxDay)), true, user));
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("3500.00"), "Investments",
                    tag + "Mutual Fund Systematic Investment Dividend", LocalDate.of(year, month, Math.min(15, maxDay)), true, user));
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("3000.00"), "Other Income",
                    tag + "Hackathon 2nd Place Prize Award", LocalDate.of(year, month, Math.min(22, maxDay)), true, user));

            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("15000.00"), "Rent",
                    tag + "Student Housing & Apartment Monthly Rent", LocalDate.of(year, month, Math.min(2, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("4600.00"), "Food",
                    tag + "Organic Groceries & Farm Produce", LocalDate.of(year, month, Math.min(4, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("3400.00"), "Bills",
                    tag + "Power Utility & Municipal Water Bill", LocalDate.of(year, month, Math.min(5, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2500.00"), "Transportation",
                    tag + "Metro Smartcard & Cab Rides", LocalDate.of(year, month, Math.min(7, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("3200.00"), "Education",
                    tag + "Database Architecture Handbook & Cheatsheets", LocalDate.of(year, month, Math.min(9, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2900.00"), "Food",
                    tag + "Cafe Working Sessions & Team Lunches", LocalDate.of(year, month, Math.min(12, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2100.00"), "Entertainment",
                    tag + "Music Festival Concert Tickets", LocalDate.of(year, month, Math.min(14, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("4800.00"), "Shopping",
                    tag + "Festival Apparel & Campus Wardrobe", LocalDate.of(year, month, Math.min(16, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1400.00"), "Healthcare",
                    tag + "Dental Care & Hygiene Products", LocalDate.of(year, month, Math.min(18, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2500.00"), "Travel",
                    tag + "Express Train Tickets for Hometown Family Visit", LocalDate.of(year, month, Math.min(20, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1200.00"), "Bills",
                    tag + "Cloud Server Hosting & Domain Renewal", LocalDate.of(year, month, Math.min(21, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1500.00"), "Healthcare",
                    tag + "Monthly Gym & Sports Facility Pass", LocalDate.of(year, month, Math.min(23, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("799.00"), "Bills",
                    tag + "Mobile Cellular 5G Data Plan", LocalDate.of(year, month, Math.min(25, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2200.00"), "Food",
                    tag + "Italian Bistro Celebration Dinner", LocalDate.of(year, month, Math.min(26, maxDay)), true, user));
        } else {
            // Month 3 (Target / Current month)
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("65000.00"), "Salary",
                    tag + "Monthly Software Engineer Stipend / Salary", LocalDate.of(year, month, Math.min(1, maxDay)), true, user));
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("16500.00"), "Freelance",
                    tag + "Full-Stack Web Application for University Hackathon", LocalDate.of(year, month, Math.min(6, maxDay)), true, user));
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("3800.00"), "Investments",
                    tag + "Quarterly Mutual Fund Growth Dividend", LocalDate.of(year, month, Math.min(15, maxDay)), true, user));
            list.add(new Transaction(TransactionType.INCOME, new BigDecimal("2500.00"), "Other Income",
                    tag + "Peer-to-Peer Data Structures Tutoring Honorarium", LocalDate.of(year, month, Math.min(22, maxDay)), true, user));

            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("15000.00"), "Rent",
                    tag + "Student Housing & Apartment Monthly Rent", LocalDate.of(year, month, Math.min(2, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("4850.00"), "Food",
                    tag + "Supermarket Grocery Shopping & Pantry Supplies", LocalDate.of(year, month, Math.min(4, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("3250.00"), "Bills",
                    tag + "Electricity & High-Speed Fiber Internet Bill", LocalDate.of(year, month, Math.min(5, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2600.00"), "Transportation",
                    tag + "City Metro Rail Smartcard Monthly Recharge", LocalDate.of(year, month, Math.min(7, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("3400.00"), "Education",
                    tag + "Semester Engineering Textbooks & Reference Guides", LocalDate.of(year, month, Math.min(9, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("3600.00"), "Food",
                    tag + "Campus Cafeteria Lunches & Study Group Coffee", LocalDate.of(year, month, Math.min(12, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1850.00"), "Entertainment",
                    tag + "Weekend Cinema Tickets & OTT Video Streaming", LocalDate.of(year, month, Math.min(14, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("4200.00"), "Shopping",
                    tag + "Winter Casual Apparel & Campus Laptop Backpack", LocalDate.of(year, month, Math.min(16, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1450.00"), "Healthcare",
                    tag + "Preventative Dental Checkup & Multivitamins", LocalDate.of(year, month, Math.min(18, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2100.00"), "Travel",
                    tag + "Intercity Express Train Ticket for Weekend Hometown Visit", LocalDate.of(year, month, Math.min(20, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1200.00"), "Bills",
                    tag + "Cloud Developer VPS Hosting & Domain Renewal", LocalDate.of(year, month, Math.min(21, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1500.00"), "Healthcare",
                    tag + "University Gymnasium & Sports Complex Pass", LocalDate.of(year, month, Math.min(23, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("799.00"), "Bills",
                    tag + "Mobile Cellular 5G Postpaid Unlimited Plan", LocalDate.of(year, month, Math.min(25, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2400.00"), "Food",
                    tag + "Team Project Celebration & Weekend Diners", LocalDate.of(year, month, Math.min(26, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("2800.00"), "Education",
                    tag + "Oracle Java SE Certified Associate Exam Voucher", LocalDate.of(year, month, Math.min(27, maxDay)), true, user));
            list.add(new Transaction(TransactionType.EXPENSE, new BigDecimal("1950.00"), "Shopping",
                    tag + "Ergonomic Optical Mouse & USB-C Multiport Hub", LocalDate.of(year, month, Math.min(28, maxDay)), true, user));
        }

        return list;
    }

    @Transactional
    public int purgeSampleData(User user) {
        return transactionRepository.deleteByUserIdAndIsDemoTrue(user.getId());
    }
}
