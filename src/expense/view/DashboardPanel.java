package expense.view;

import expense.controller.ExpenseController;
import expense.exception.StorageException;
import expense.model.*;
import expense.service.ReportService;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;
import expense.util.UIUtil;
import expense.view.chart.BarChartPanel;
import expense.view.chart.PieChartPanel;
import expense.view.chart.TrendLineChartPanel;
import expense.view.component.MetricCard;
import expense.view.component.ModernProgressBar;
import expense.view.component.TransactionTableModel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Dashboard screen displaying high-level financial KPIs, charts, alerts,
 * and recent transaction summaries.
 */
public class DashboardPanel extends JPanel {

    private final ExpenseController controller;
    private final Consumer<String> navigationHandler;

    // Filter controls
    private JComboBox<Integer> cbYear;
    private JComboBox<String> cbMonth;

    // KPI Cards
    private MetricCard cardIncome;
    private MetricCard cardExpense;
    private MetricCard cardBalance;
    private MetricCard cardSavings;
    private MetricCard cardBudget;
    private MetricCard cardRemaining;

    // Charts
    private PieChartPanel pieChart;
    private BarChartPanel barChart;
    private TrendLineChartPanel trendChart;

    // Budget Progress & Alert Section
    private JLabel lblBudgetAlertText;
    private JLabel lblBudgetUtilText;
    private ModernProgressBar budgetProgressBar;
    private JPanel alertBanner;
    private JPanel demoBanner;

    // Recent Transactions
    private JTable recentTable;
    private TransactionTableModel tableModel;

    // Quick insights
    private JLabel lblHighestCategory;
    private JLabel lblSavingsRate;

    public DashboardPanel(ExpenseController controller, Consumer<String> navigationHandler) {
        this.controller = controller;
        this.navigationHandler = navigationHandler;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtil.COLOR_MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // TOP: Header with Month/Year picker and Quick Action buttons
        add(createTopBar(), BorderLayout.NORTH);

        // CENTER: Scrollable Content with Cards, Charts, and Recent Table
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // 0. Demo Data Banner (if active)
        demoBanner = createDemoBanner();
        contentPanel.add(demoBanner);
        contentPanel.add(Box.createVerticalStrut(14));

        // 1. KPI Cards Row
        contentPanel.add(createKpiGrid());
        contentPanel.add(Box.createVerticalStrut(16));

        // 2. Budget Alert Banner
        contentPanel.add(createBudgetAlertBanner());
        contentPanel.add(Box.createVerticalStrut(16));

        // 3. Charts Row (Income vs Expense Bar + Category Pie)
        contentPanel.add(createChartsRow());
        contentPanel.add(Box.createVerticalStrut(16));

        // 4. Annual Trend Line Chart
        contentPanel.add(createTrendRow());
        contentPanel.add(Box.createVerticalStrut(16));

        // 5. Recent Transactions & Quick Insights
        contentPanel.add(createBottomRow());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);

        // Left: Title and Subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Financial Dashboard");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        titlePanel.add(lblTitle);

        JLabel lblSub = new JLabel("Real-time overview of your income, expenses, and savings");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        titlePanel.add(lblSub);

        bar.add(titlePanel, BorderLayout.WEST);

        // Right: Month/Year Selector and Quick Add Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JLabel lblFilter = new JLabel("Period:");
        lblFilter.setFont(UIUtil.FONT_REGULAR_BOLD);
        lblFilter.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        actions.add(lblFilter);

        YearMonth current = YearMonth.now();
        String[] months = new String[] {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        cbMonth = UIUtil.createStyledComboBox(months);
        cbMonth.setSelectedIndex(current.getMonthValue() - 1);
        cbMonth.addActionListener(e -> refreshData());
        actions.add(cbMonth);

        Integer[] years = new Integer[] { 2023, 2024, 2025, 2026, 2027 };
        cbYear = UIUtil.createStyledComboBox(years);
        cbYear.setSelectedItem(current.getYear());
        cbYear.addActionListener(e -> refreshData());
        actions.add(cbYear);

        // Quick Add Income Button
        JButton btnQuickIncome = UIUtil.createSuccessButton("+ Add Income");
        btnQuickIncome.addActionListener(e -> {
            if (navigationHandler != null) {
                navigationHandler.accept("ADD_TRANSACTION_INCOME");
            }
        });
        actions.add(btnQuickIncome);

        // Quick Add Expense Button
        JButton btnQuickExpense = UIUtil.createDangerButton("- Add Expense");
        btnQuickExpense.addActionListener(e -> {
            if (navigationHandler != null) {
                navigationHandler.accept("ADD_TRANSACTION_EXPENSE");
            }
        });
        actions.add(btnQuickExpense);

        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel createKpiGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);

        cardIncome = new MetricCard("Monthly Income", "₹ 0.00", "Earnings for selected period", "▲", UIUtil.COLOR_INCOME);
        cardExpense = new MetricCard("Monthly Expenses", "₹ 0.00", "Outflow for selected period", "▼", UIUtil.COLOR_EXPENSE);
        cardBalance = new MetricCard("Current Balance", "₹ 0.00", "All-time cash on hand", "★", UIUtil.COLOR_PRIMARY);

        cardSavings = new MetricCard("Monthly Savings", "₹ 0.00", "Savings Rate: 0%", "◆", UIUtil.COLOR_PURPLE);
        cardBudget = new MetricCard("Monthly Budget", "₹ 0.00", "Allocated limit", "⏱", UIUtil.COLOR_WARNING);
        cardRemaining = new MetricCard("Remaining Budget", "₹ 0.00", "Available allowance", "✔", UIUtil.COLOR_PRIMARY);

        grid.add(cardIncome);
        grid.add(cardExpense);
        grid.add(cardBalance);
        grid.add(cardSavings);
        grid.add(cardBudget);
        grid.add(cardRemaining);

        return grid;
    }

    private JPanel createDemoBanner() {
        JPanel banner = new JPanel(new BorderLayout(16, 0));
        banner.setBackground(new Color(254, 243, 199)); // Warm amber
        banner.setBorder(new CompoundBorder(
                new LineBorder(new Color(245, 158, 11), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        left.setOpaque(false);

        JLabel lblBadge = new JLabel("DEMO DATA ACTIVE");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setBackground(new Color(217, 119, 6));
        lblBadge.setOpaque(true);
        lblBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        left.add(lblBadge);

        JLabel lblText = new JLabel("Currently previewing realistic sample transactions & budgets. Cards, charts and alerts reflect demo data.");
        lblText.setFont(UIUtil.FONT_REGULAR);
        lblText.setForeground(new Color(120, 53, 15));
        left.add(lblText);

        banner.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnRemoveDemo = UIUtil.createDangerButton("Remove Demo Data");
        btnRemoveDemo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemoveDemo.addActionListener(e -> onRemoveDemoData());
        actions.add(btnRemoveDemo);

        banner.add(actions, BorderLayout.EAST);
        return banner;
    }

    private void onRemoveDemoData() {
        boolean confirm = UIUtil.showConfirm(this,
                "Remove all sample demo transactions and start with your own personal records?\n" +
                "Any custom transactions you created will NOT be deleted.",
                "Confirm Remove Demo Data");
        if (confirm) {
            try {
                int count = controller.removeSampleData();
                UIUtil.showSuccess(this, "Successfully removed " + count + " demo records.\nYour ledger is now clean and ready for personal transactions.");
            } catch (StorageException ex) {
                UIUtil.showError(this, "Failed to remove demo data: " + ex.getMessage());
            }
        }
    }

    private JPanel createBudgetAlertBanner() {
        alertBanner = new JPanel(new BorderLayout(16, 0));
        alertBanner.setBackground(UIUtil.COLOR_CARD_BG);
        alertBanner.setBorder(new CompoundBorder(
                new LineBorder(UIUtil.COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        lblBudgetAlertText = new JLabel("Budget Status: Normal");
        lblBudgetAlertText.setFont(UIUtil.FONT_REGULAR_BOLD);
        left.add(lblBudgetAlertText);

        lblBudgetUtilText = new JLabel("Utilization: 0% of allocated budget");
        lblBudgetUtilText.setFont(UIUtil.FONT_SMALL);
        lblBudgetUtilText.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        left.add(lblBudgetUtilText);

        alertBanner.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        right.setOpaque(false);

        budgetProgressBar = new ModernProgressBar();
        budgetProgressBar.setPreferredSize(new Dimension(240, 16));
        right.add(budgetProgressBar);

        JButton btnManageBudget = UIUtil.createSecondaryButton("Manage Budget");
        btnManageBudget.addActionListener(e -> {
            if (navigationHandler != null) navigationHandler.accept("BUDGET");
        });
        right.add(btnManageBudget);

        alertBanner.add(right, BorderLayout.EAST);
        return alertBanner;
    }

    private JPanel createChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);

        // Left: Bar chart (Income vs Expense)
        JPanel barCard = UIUtil.createCardPanel();
        barCard.setLayout(new BorderLayout());
        barChart = new BarChartPanel();
        barCard.add(barChart, BorderLayout.CENTER);
        row.add(barCard);

        // Right: Doughnut chart (Expense Distribution by Category)
        JPanel pieCard = UIUtil.createCardPanel();
        pieCard.setLayout(new BorderLayout());
        pieChart = new PieChartPanel();
        pieCard.add(pieChart, BorderLayout.CENTER);
        row.add(pieCard);

        return row;
    }

    private JPanel createTrendRow() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new BorderLayout());
        trendChart = new TrendLineChartPanel();
        card.add(trendChart, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBottomRow() {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);

        // Left / Main: Recent Transactions
        JPanel tableCard = UIUtil.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel lblRecentTitle = new JLabel("Recent Transactions");
        lblRecentTitle.setFont(UIUtil.FONT_SUBTITLE);
        lblRecentTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        tableHeader.add(lblRecentTitle, BorderLayout.WEST);

        JButton btnViewAll = UIUtil.createSecondaryButton("View All Transactions →");
        btnViewAll.addActionListener(e -> {
            if (navigationHandler != null) navigationHandler.accept("TRANSACTIONS");
        });
        tableHeader.add(btnViewAll, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        tableModel = new TransactionTableModel();
        recentTable = new JTable(tableModel);
        UIUtil.styleTable(recentTable);
        TransactionTableModel.applyRenderers(recentTable);

        JScrollPane tableScroll = new JScrollPane(recentTable);
        tableScroll.setBorder(new LineBorder(UIUtil.COLOR_BORDER, 1));
        tableScroll.setPreferredSize(new Dimension(500, 180));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        row.add(tableCard, BorderLayout.CENTER);

        // Right: Quick Insights summary card
        JPanel insightsCard = UIUtil.createCardPanel();
        insightsCard.setPreferredSize(new Dimension(280, 220));
        insightsCard.setLayout(new GridLayout(4, 1, 0, 8));

        JLabel lblInsightHead = new JLabel("Monthly Insights");
        lblInsightHead.setFont(UIUtil.FONT_SUBTITLE);
        lblInsightHead.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        insightsCard.add(lblInsightHead);

        JPanel p1 = new JPanel(new GridLayout(2, 1));
        p1.setOpaque(false);
        JLabel t1 = new JLabel("Top Spending Category");
        t1.setFont(UIUtil.FONT_SMALL);
        t1.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        lblHighestCategory = new JLabel("Food (₹ 0.00)");
        lblHighestCategory.setFont(UIUtil.FONT_REGULAR_BOLD);
        p1.add(t1);
        p1.add(lblHighestCategory);
        insightsCard.add(p1);

        JPanel p2 = new JPanel(new GridLayout(2, 1));
        p2.setOpaque(false);
        JLabel t2 = new JLabel("Savings Rate");
        t2.setFont(UIUtil.FONT_SMALL);
        t2.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        lblSavingsRate = new JLabel("0.0% of total income");
        lblSavingsRate.setFont(UIUtil.FONT_REGULAR_BOLD);
        p2.add(t2);
        p2.add(lblSavingsRate);
        insightsCard.add(p2);

        JButton btnReports = UIUtil.createPrimaryButton("View Full Analytics");
        btnReports.addActionListener(e -> {
            if (navigationHandler != null) navigationHandler.accept("REPORTS");
        });
        insightsCard.add(btnReports);

        row.add(insightsCard, BorderLayout.EAST);

        return row;
    }

    public void refreshData() {
        int year = cbYear.getSelectedItem() != null ? (Integer) cbYear.getSelectedItem() : YearMonth.now().getYear();
        int month = cbMonth.getSelectedIndex() + 1;

        FinancialSummary summary = controller.getReportService().getMonthlySummary(
                year, month, controller.getTransactionService(), controller.getBudgetService());

        BigDecimal allTimeBalance = controller.getReportService().getAllTimeBalance(controller.getTransactionService());

        // Update KPI Cards (clearly distinguishing monthly from all-time metrics)
        cardIncome.setValue(CurrencyUtil.format(summary.getTotalIncome()));
        cardIncome.setSubtitle("Earnings in " + DateUtil.formatMonthYear(year, month));

        cardExpense.setValue(CurrencyUtil.format(summary.getTotalExpense()));
        cardExpense.setSubtitle("Outflow in " + DateUtil.formatMonthYear(year, month));

        cardBalance.setValue(CurrencyUtil.format(allTimeBalance));
        cardBalance.setSubtitle("All-Time Net Cash on Hand");

        cardSavings.setValue(CurrencyUtil.format(summary.getMonthlySavings()));
        cardSavings.setSubtitle("Savings Rate: " + summary.getSavingsPercentage() + "%");

        cardBudget.setValue(CurrencyUtil.format(summary.getMonthlyBudget()));
        cardRemaining.setValue(CurrencyUtil.format(summary.getRemainingBudget()));

        if (summary.getRemainingBudget().compareTo(BigDecimal.ZERO) < 0) {
            cardRemaining.setSubtitle("Budget exceeded by " + CurrencyUtil.format(summary.getRemainingBudget().abs()));
            cardRemaining.setSubtitleColor(UIUtil.COLOR_EXPENSE);
        } else {
            cardRemaining.setSubtitle("Within budget limit");
            cardRemaining.setSubtitleColor(UIUtil.COLOR_INCOME);
        }

        // Update Budget Progress & Alerts
        BudgetStatus overallStatus = controller.getBudgetService().getOverallBudgetStatus(
                year, month, controller.getTransactionService());
        budgetProgressBar.setPercentage(overallStatus.getUtilizationPercentage());

        if (overallStatus.getLimit().compareTo(BigDecimal.ZERO) <= 0) {
            lblBudgetAlertText.setText("Budget Status: No Monthly Budget Configured");
            lblBudgetAlertText.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
            lblBudgetUtilText.setText("Click 'Manage Budget' to define a spending limit for this month.");
            alertBanner.setBackground(UIUtil.COLOR_CARD_BG);
        } else if (overallStatus.isExceeded()) {
            lblBudgetAlertText.setText("CRITICAL ALERT: Monthly Budget Exceeded!");
            lblBudgetAlertText.setForeground(UIUtil.COLOR_EXPENSE);
            lblBudgetUtilText.setText(String.format("Spent %s of %s (%.1f%% utilized)",
                    CurrencyUtil.format(overallStatus.getSpent()),
                    CurrencyUtil.format(overallStatus.getLimit()),
                    overallStatus.getUtilizationPercentage()));
            alertBanner.setBackground(UIUtil.COLOR_EXPENSE_LIGHT);
        } else if (overallStatus.isWarning()) {
            lblBudgetAlertText.setText("WARNING: Budget Utilization at 80%+");
            lblBudgetAlertText.setForeground(UIUtil.COLOR_WARNING);
            lblBudgetUtilText.setText(String.format("Spent %s of %s (%.1f%% utilized)",
                    CurrencyUtil.format(overallStatus.getSpent()),
                    CurrencyUtil.format(overallStatus.getLimit()),
                    overallStatus.getUtilizationPercentage()));
            alertBanner.setBackground(UIUtil.COLOR_WARNING_LIGHT);
        } else {
            lblBudgetAlertText.setText("Budget Status: Spending Under Control");
            lblBudgetAlertText.setForeground(UIUtil.COLOR_INCOME);
            lblBudgetUtilText.setText(String.format("Spent %s of %s (%.1f%% utilized)",
                    CurrencyUtil.format(overallStatus.getSpent()),
                    CurrencyUtil.format(overallStatus.getLimit()),
                    overallStatus.getUtilizationPercentage()));
            alertBanner.setBackground(UIUtil.COLOR_INCOME_LIGHT);
        }

        // Update Charts
        LocalDate start = DateUtil.getStartOfMonth(year, month);
        LocalDate end = DateUtil.getEndOfMonth(year, month);

        Map<String, BigDecimal> catExpenses = controller.getReportService().getExpenseCategoryBreakdown(
                start, end, controller.getTransactionService());
        pieChart.setData(catExpenses, "Total Expense");

        barChart.setComparisonData(summary.getTotalIncome(), summary.getTotalExpense(),
                "Income vs Expenses - " + DateUtil.formatMonthYear(year, month));

        List<ReportService.MonthlyTrendPoint> trend = controller.getReportService().getYearlyTrend(
                year, controller.getTransactionService());
        trendChart.setTrendData(trend, year + " Annual Financial Trend");

        // Update Recent Transactions Table
        List<Transaction> recent = controller.getReportService().getRecentTransactions(8, controller.getTransactionService());
        tableModel.setTransactions(recent);

        // Update Insights
        if (summary.getHighestSpendingAmount().compareTo(BigDecimal.ZERO) > 0) {
            lblHighestCategory.setText(summary.getHighestSpendingCategory() + " (" +
                    CurrencyUtil.format(summary.getHighestSpendingAmount()) + ")");
        } else {
            lblHighestCategory.setText("No expenses recorded");
        }
        lblSavingsRate.setText(summary.getSavingsPercentage() + "% of total earnings");

        // Toggle Demo Banner visibility
        if (demoBanner != null) {
            demoBanner.setVisible(controller.hasSampleData());
        }
    }
}
