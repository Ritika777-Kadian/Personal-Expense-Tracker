package expense.view;

import expense.controller.ExpenseController;
import expense.exception.StorageException;
import expense.exception.ValidationException;
import expense.model.Budget;
import expense.model.BudgetStatus;
import expense.model.TransactionType;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;
import expense.util.UIUtil;
import expense.view.component.ModernProgressBar;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Screen for managing monthly and category spending limits with threshold alerts and progress tracking.
 */
public class BudgetPanel extends JPanel {

    private final ExpenseController controller;

    private JComboBox<Integer> cbYear;
    private JComboBox<String> cbMonth;

    // Overall Budget Display
    private JLabel lblOverallLimit;
    private JLabel lblOverallSpent;
    private JLabel lblOverallRemaining;
    private JLabel lblOverallUtil;
    private ModernProgressBar overallProgressBar;
    private JPanel overallAlertCard;
    private JLabel lblOverallAlertStatus;

    // Forms
    private JTextField tfOverallBudget;
    private JComboBox<String> cbExpenseCategories;
    private JTextField tfCategoryBudget;

    // Category Budgets Table
    private DefaultTableModel categoryTableModel;
    private JTable categoryTable;

    public BudgetPanel(ExpenseController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtil.COLOR_MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // TOP: Header with Month/Year picker
        add(createHeader(), BorderLayout.NORTH);

        // CENTER: Scrollable view with Overall Card + Setup Forms + Category Progress Table
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // 1. Overall Monthly Budget Card
        contentPanelAdd(content, createOverallBudgetCard());
        content.add(Box.createVerticalStrut(16));

        // 2. Budget Config Forms (Set Overall + Set Category)
        contentPanelAdd(content, createConfigFormsRow());
        content.add(Box.createVerticalStrut(16));

        // 3. Category Budgets Table Card
        contentPanelAdd(content, createCategoryTableCard());

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        add(sp, BorderLayout.CENTER);
    }

    private void contentPanelAdd(JPanel parent, JComponent comp) {
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(comp);
    }

    private JPanel createHeader() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Budget Management & Alerts");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        titlePanel.add(lblTitle);

        JLabel lblSub = new JLabel("Define spending limits, monitor category allowances, and track threshold alerts");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        titlePanel.add(lblSub);

        bar.add(titlePanel, BorderLayout.WEST);

        // Month / Year selectors
        JPanel picker = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        picker.setOpaque(false);

        JLabel lblPeriod = new JLabel("Budget Month:");
        lblPeriod.setFont(UIUtil.FONT_REGULAR_BOLD);
        picker.add(lblPeriod);

        String[] months = new String[] {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        cbMonth = UIUtil.createStyledComboBox(months);
        cbMonth.setSelectedIndex(YearMonth.now().getMonthValue() - 1);
        cbMonth.addActionListener(e -> refreshData());
        picker.add(cbMonth);

        Integer[] years = new Integer[] { 2023, 2024, 2025, 2026, 2027 };
        cbYear = UIUtil.createStyledComboBox(years);
        cbYear.setSelectedItem(YearMonth.now().getYear());
        cbYear.addActionListener(e -> refreshData());
        picker.add(cbYear);

        bar.add(picker, BorderLayout.EAST);
        return bar;
    }

    private JPanel createOverallBudgetCard() {
        overallAlertCard = UIUtil.createCardPanel();
        overallAlertCard.setLayout(new BorderLayout(16, 12));

        // Left / Top: Title & Alert Message
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblCardTitle = new JLabel("OVERALL MONTHLY BUDGET PERFORMANCE");
        lblCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCardTitle.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        top.add(lblCardTitle, BorderLayout.WEST);

        lblOverallAlertStatus = new JLabel("Status: Normal (<80%)");
        lblOverallAlertStatus.setFont(UIUtil.FONT_REGULAR_BOLD);
        top.add(lblOverallAlertStatus, BorderLayout.EAST);

        overallAlertCard.add(top, BorderLayout.NORTH);

        // Center: Metric columns
        JPanel metrics = new JPanel(new GridLayout(1, 4, 16, 0));
        metrics.setOpaque(false);

        metrics.add(createMiniMetric("Budget Limit", lblOverallLimit = new JLabel("₹ 0.00"), UIUtil.COLOR_TEXT_PRIMARY));
        metrics.add(createMiniMetric("Amount Spent", lblOverallSpent = new JLabel("₹ 0.00"), UIUtil.COLOR_EXPENSE));
        metrics.add(createMiniMetric("Remaining", lblOverallRemaining = new JLabel("₹ 0.00"), UIUtil.COLOR_INCOME));
        metrics.add(createMiniMetric("Utilization", lblOverallUtil = new JLabel("0.0%"), UIUtil.COLOR_PRIMARY));

        overallAlertCard.add(metrics, BorderLayout.CENTER);

        // Bottom: Progress bar
        JPanel progWrap = new JPanel(new BorderLayout(8, 0));
        progWrap.setOpaque(false);
        overallProgressBar = new ModernProgressBar();
        overallProgressBar.setPreferredSize(new Dimension(500, 16));
        progWrap.add(overallProgressBar, BorderLayout.CENTER);

        overallAlertCard.add(progWrap, BorderLayout.SOUTH);

        return overallAlertCard;
    }

    private JPanel createMiniMetric(String title, JLabel valueLabel, Color valColor) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 4));
        p.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(UIUtil.FONT_SMALL);
        lblT.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        p.add(lblT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(valColor);
        p.add(valueLabel);

        return p;
    }

    private JPanel createConfigFormsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);

        // Form 1: Set Overall Budget
        JPanel p1 = UIUtil.createCardPanel();
        p1.setLayout(new BorderLayout(0, 10));

        JLabel t1 = new JLabel("Set / Update Overall Budget");
        t1.setFont(UIUtil.FONT_SUBTITLE);
        t1.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        p1.add(t1, BorderLayout.NORTH);

        JPanel f1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        f1.setOpaque(false);
        f1.add(new JLabel("Monthly Limit (" + CurrencyUtil.getActiveSymbol() + "):"));
        tfOverallBudget = UIUtil.createStyledTextField(10);
        f1.add(tfOverallBudget);

        JButton btnSaveOverall = UIUtil.createPrimaryButton("Save Limit");
        btnSaveOverall.addActionListener(e -> onSaveOverallBudget());
        f1.add(btnSaveOverall);
        p1.add(f1, BorderLayout.CENTER);

        row.add(p1);

        // Form 2: Set Category-Wise Budget
        JPanel p2 = UIUtil.createCardPanel();
        p2.setLayout(new BorderLayout(0, 10));

        JLabel t2 = new JLabel("Set / Update Category Budget");
        t2.setFont(UIUtil.FONT_SUBTITLE);
        t2.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        p2.add(t2, BorderLayout.NORTH);

        JPanel f2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        f2.setOpaque(false);
        f2.add(new JLabel("Category:"));
        cbExpenseCategories = UIUtil.createStyledComboBox(new String[0]);
        f2.add(cbExpenseCategories);

        f2.add(new JLabel("Limit:"));
        tfCategoryBudget = UIUtil.createStyledTextField(8);
        f2.add(tfCategoryBudget);

        JButton btnSaveCat = UIUtil.createPrimaryButton("Set Category Limit");
        btnSaveCat.addActionListener(e -> onSaveCategoryBudget());
        f2.add(btnSaveCat);
        p2.add(f2, BorderLayout.CENTER);

        row.add(p2);

        return row;
    }

    private JPanel createCategoryTableCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel("Category-Wise Budget Progress & Utilization");
        title.setFont(UIUtil.FONT_SUBTITLE);
        title.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        head.add(title, BorderLayout.WEST);

        JButton btnDelete = UIUtil.createSecondaryButton("Remove Selected Budget");
        btnDelete.addActionListener(e -> onDeleteCategoryBudget());
        head.add(btnDelete, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);

        String[] cols = { "Category", "Budget Limit", "Amount Spent", "Remaining", "Utilization %", "Status Alert" };
        categoryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        categoryTable = new JTable(categoryTableModel);
        UIUtil.styleTable(categoryTable);

        // Alignment renderers
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        right.setFont(UIUtil.FONT_REGULAR_BOLD);
        right.setBorder(new EmptyBorder(0, 8, 0, 8));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        center.setFont(UIUtil.FONT_REGULAR_BOLD);

        categoryTable.getColumnModel().getColumn(1).setCellRenderer(right);
        categoryTable.getColumnModel().getColumn(2).setCellRenderer(right);
        categoryTable.getColumnModel().getColumn(3).setCellRenderer(right);
        categoryTable.getColumnModel().getColumn(4).setCellRenderer(center);
        categoryTable.getColumnModel().getColumn(5).setCellRenderer(new BudgetStatusRenderer());

        JScrollPane sp = new JScrollPane(categoryTable);
        sp.setBorder(new LineBorder(UIUtil.COLOR_BORDER, 1));
        sp.setPreferredSize(new Dimension(500, 220));
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    private void onSaveOverallBudget() {
        int year = (Integer) cbYear.getSelectedItem();
        int month = cbMonth.getSelectedIndex() + 1;

        try {
            BigDecimal limit = CurrencyUtil.parseAmount(tfOverallBudget.getText());
            controller.getBudgetService().setBudget(Budget.OVERALL_CATEGORY, limit, year, month);
            controller.notifyDataChanged();
            tfOverallBudget.setText("");
            UIUtil.showSuccess(this, "Overall monthly budget of " + CurrencyUtil.format(limit) + " set successfully!");
        } catch (ValidationException | IllegalArgumentException ex) {
            UIUtil.showError(this, ex.getMessage());
        } catch (StorageException ex) {
            UIUtil.showError(this, "Storage Error: " + ex.getMessage());
        }
    }

    private void onSaveCategoryBudget() {
        int year = (Integer) cbYear.getSelectedItem();
        int month = cbMonth.getSelectedIndex() + 1;
        String category = (String) cbExpenseCategories.getSelectedItem();

        if (category == null || category.isBlank()) {
            UIUtil.showError(this, "Please select an expense category.");
            return;
        }

        try {
            BigDecimal limit = CurrencyUtil.parseAmount(tfCategoryBudget.getText());
            controller.getBudgetService().setBudget(category, limit, year, month);
            controller.notifyDataChanged();
            tfCategoryBudget.setText("");
            UIUtil.showSuccess(this, "Budget of " + CurrencyUtil.format(limit) + " for category '" + category + "' saved!");
        } catch (ValidationException | IllegalArgumentException ex) {
            UIUtil.showError(this, ex.getMessage());
        } catch (StorageException ex) {
            UIUtil.showError(this, "Storage Error: " + ex.getMessage());
        }
    }

    private void onDeleteCategoryBudget() {
        int row = categoryTable.getSelectedRow();
        if (row < 0) {
            UIUtil.showError(this, "Please select a category budget to remove.");
            return;
        }

        String catName = (String) categoryTable.getValueAt(row, 0);
        int year = (Integer) cbYear.getSelectedItem();
        int month = cbMonth.getSelectedIndex() + 1;

        boolean confirm = UIUtil.showConfirm(this,
                "Remove budget limit for category '" + catName + "' in " + DateUtil.formatMonthYear(year, month) + "?",
                "Confirm Removal");

        if (confirm) {
            try {
                controller.getBudgetService().deleteBudget(catName, year, month);
                controller.notifyDataChanged();
                UIUtil.showSuccess(this, "Budget limit removed.");
            } catch (StorageException ex) {
                UIUtil.showError(this, "Storage Error: " + ex.getMessage());
            }
        }
    }

    public void refreshData() {
        int year = cbYear.getSelectedItem() != null ? (Integer) cbYear.getSelectedItem() : YearMonth.now().getYear();
        int month = cbMonth.getSelectedIndex() + 1;

        // Populate Expense Category Dropdown
        cbExpenseCategories.removeAllItems();
        for (String c : controller.getCategoryService().getCategoryNamesByType(TransactionType.EXPENSE)) {
            cbExpenseCategories.addItem(c);
        }

        // Overall Budget Status
        BudgetStatus overall = controller.getBudgetService().getOverallBudgetStatus(
                year, month, controller.getTransactionService());

        lblOverallLimit.setText(CurrencyUtil.format(overall.getLimit()));
        lblOverallSpent.setText(CurrencyUtil.format(overall.getSpent()));
        lblOverallRemaining.setText(CurrencyUtil.format(overall.getRemaining()));
        lblOverallUtil.setText(String.format("%.1f%%", overall.getUtilizationPercentage()));
        overallProgressBar.setPercentage(overall.getUtilizationPercentage());

        if (overall.getLimit().compareTo(BigDecimal.ZERO) <= 0) {
            lblOverallAlertStatus.setText("Status: Not Configured");
            lblOverallAlertStatus.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
            overallAlertCard.setBackground(Color.WHITE);
        } else if (overall.isExceeded()) {
            lblOverallAlertStatus.setText("CRITICAL: Limit Exceeded (>100%)!");
            lblOverallAlertStatus.setForeground(UIUtil.COLOR_EXPENSE);
            overallAlertCard.setBackground(UIUtil.COLOR_EXPENSE_LIGHT);
        } else if (overall.isWarning()) {
            lblOverallAlertStatus.setText("WARNING: Approaching Limit (>=80%)!");
            lblOverallAlertStatus.setForeground(UIUtil.COLOR_WARNING);
            overallAlertCard.setBackground(UIUtil.COLOR_WARNING_LIGHT);
        } else {
            lblOverallAlertStatus.setText("Status: Normal (<80%)");
            lblOverallAlertStatus.setForeground(UIUtil.COLOR_INCOME);
            overallAlertCard.setBackground(UIUtil.COLOR_INCOME_LIGHT);
        }

        // Category Budgets Table
        categoryTableModel.setRowCount(0);
        List<BudgetStatus> catStatuses = controller.getBudgetService().getCategoryBudgetStatuses(
                year, month, controller.getTransactionService());

        for (BudgetStatus bs : catStatuses) {
            categoryTableModel.addRow(new Object[] {
                    bs.getCategory(),
                    CurrencyUtil.format(bs.getLimit()),
                    CurrencyUtil.format(bs.getSpent()),
                    CurrencyUtil.format(bs.getRemaining()),
                    String.format("%.1f%%", bs.getUtilizationPercentage()),
                    bs.getAlertLevel().getTitle()
            });
        }
    }

    private static class BudgetStatusRenderer extends DefaultTableCellRenderer {
        public BudgetStatusRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = value != null ? value.toString() : "";
            if ("Exceeded".equalsIgnoreCase(text)) {
                setForeground(UIUtil.COLOR_EXPENSE);
                setText("● EXCEEDED");
            } else if ("Warning".equalsIgnoreCase(text)) {
                setForeground(UIUtil.COLOR_WARNING);
                setText("▲ WARNING (≥80%)");
            } else {
                setForeground(UIUtil.COLOR_INCOME);
                setText("✔ NORMAL");
            }
            return this;
        }
    }
}
