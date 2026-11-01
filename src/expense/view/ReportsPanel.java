package expense.view;

import expense.controller.ExpenseController;
import expense.exception.StorageException;
import expense.model.BudgetStatus;
import expense.model.FinancialSummary;
import expense.model.Transaction;
import expense.service.ReportService;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;
import expense.util.UIUtil;
import expense.view.chart.BarChartPanel;
import expense.view.chart.PieChartPanel;
import expense.view.chart.TrendLineChartPanel;
import expense.view.component.MetricCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Screen providing extensive financial reporting, category breakdown tables,
 * comparative charts, and analytical CSV export.
 */
public class ReportsPanel extends JPanel {

    private final ExpenseController controller;

    private JComboBox<String> cbMonth;
    private JComboBox<Integer> cbYear;
    private JTextField tfStartDate;
    private JTextField tfEndDate;
    private JRadioButton rbMonthly;
    private JRadioButton rbCustomRange;

    // Report Summary KPI Cards
    private MetricCard cardTotalIncome;
    private MetricCard cardTotalExpense;
    private MetricCard cardNetSavings;
    private MetricCard cardSavingsRate;

    // Charts
    private PieChartPanel pieChart;
    private BarChartPanel barChart;
    private TrendLineChartPanel trendChart;

    // Tables
    private DefaultTableModel categoryTableModel;
    private JTable categoryTable;

    public ReportsPanel(ExpenseController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtil.COLOR_MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // TOP: Header with Date Selection Controls & Export Button
        add(createTopControlPanel(), BorderLayout.NORTH);

        // CENTER: Scrollable Analytics
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // 1. KPI Cards Row
        content.add(createKpiRow());
        content.add(Box.createVerticalStrut(16));

        // 2. Charts Row (Category Doughnut + Bar Comparison)
        content.add(createChartsRow());
        content.add(Box.createVerticalStrut(16));

        // 3. Category Breakdown Table Card
        content.add(createCategoryTableCard());
        content.add(Box.createVerticalStrut(16));

        // 4. Annual 12-Month Trend Line Chart
        content.add(createTrendRow());

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        add(sp, BorderLayout.CENTER);
    }

    private JPanel createTopControlPanel() {
        JPanel bar = new JPanel(new BorderLayout(16, 10));
        bar.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Reports & Financial Analytics");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        titlePanel.add(lblTitle);

        JLabel lblSub = new JLabel("Analyze expenditures by category, review historical trends, and export analytical reports");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        titlePanel.add(lblSub);

        bar.add(titlePanel, BorderLayout.NORTH);

        // Controls Bar: Radio buttons (Month vs Custom Range) + Controls + Export Button
        JPanel filterRow = UIUtil.createCardPanel();
        filterRow.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 4));

        rbMonthly = new JRadioButton("By Month:", true);
        rbMonthly.setFont(UIUtil.FONT_REGULAR_BOLD);
        rbMonthly.setOpaque(false);

        rbCustomRange = new JRadioButton("Custom Date Range:");
        rbCustomRange.setFont(UIUtil.FONT_REGULAR_BOLD);
        rbCustomRange.setOpaque(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbMonthly);
        bg.add(rbCustomRange);

        String[] months = new String[] {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        cbMonth = UIUtil.createStyledComboBox(months);
        cbMonth.setSelectedIndex(YearMonth.now().getMonthValue() - 1);

        Integer[] years = new Integer[] { 2023, 2024, 2025, 2026, 2027 };
        cbYear = UIUtil.createStyledComboBox(years);
        cbYear.setSelectedItem(YearMonth.now().getYear());

        tfStartDate = UIUtil.createStyledTextField(8);
        tfStartDate.setText(LocalDate.now().withDayOfMonth(1).toString());
        tfStartDate.setEnabled(false);

        tfEndDate = UIUtil.createStyledTextField(8);
        tfEndDate.setText(LocalDate.now().toString());
        tfEndDate.setEnabled(false);

        rbMonthly.addActionListener(e -> {
            cbMonth.setEnabled(true);
            cbYear.setEnabled(true);
            tfStartDate.setEnabled(false);
            tfEndDate.setEnabled(false);
            refreshData();
        });

        rbCustomRange.addActionListener(e -> {
            cbMonth.setEnabled(false);
            cbYear.setEnabled(false);
            tfStartDate.setEnabled(true);
            tfEndDate.setEnabled(true);
            refreshData();
        });

        cbMonth.addActionListener(e -> { if (rbMonthly.isSelected()) refreshData(); });
        cbYear.addActionListener(e -> { if (rbMonthly.isSelected()) refreshData(); });

        filterRow.add(rbMonthly);
        filterRow.add(cbMonth);
        filterRow.add(cbYear);
        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(rbCustomRange);
        filterRow.add(new JLabel("From:"));
        filterRow.add(tfStartDate);
        filterRow.add(new JLabel("To:"));
        filterRow.add(tfEndDate);

        JButton btnApplyRange = UIUtil.createSecondaryButton("Apply Range");
        btnApplyRange.addActionListener(e -> refreshData());
        filterRow.add(btnApplyRange);

        JButton btnExport = UIUtil.createSuccessButton("Export Report to CSV");
        btnExport.addActionListener(e -> onExportReport());
        filterRow.add(btnExport);

        bar.add(filterRow, BorderLayout.CENTER);
        return bar;
    }

    private JPanel createKpiRow() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);

        cardTotalIncome = new MetricCard("Total Period Income", "₹ 0.00", "Total credits", "▲", UIUtil.COLOR_INCOME);
        cardTotalExpense = new MetricCard("Total Period Expenses", "₹ 0.00", "Total debits", "▼", UIUtil.COLOR_EXPENSE);
        cardNetSavings = new MetricCard("Net Savings", "₹ 0.00", "Surplus / Deficit", "★", UIUtil.COLOR_PRIMARY);
        cardSavingsRate = new MetricCard("Savings Rate", "0.0%", "Income retained", "◆", UIUtil.COLOR_PURPLE);

        grid.add(cardTotalIncome);
        grid.add(cardTotalExpense);
        grid.add(cardNetSavings);
        grid.add(cardSavingsRate);

        return grid;
    }

    private JPanel createChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);

        JPanel p1 = UIUtil.createCardPanel();
        p1.setLayout(new BorderLayout());
        pieChart = new PieChartPanel();
        p1.add(pieChart, BorderLayout.CENTER);
        row.add(p1);

        JPanel p2 = UIUtil.createCardPanel();
        p2.setLayout(new BorderLayout());
        barChart = new BarChartPanel();
        p2.add(barChart, BorderLayout.CENTER);
        row.add(p2);

        return row;
    }

    private JPanel createCategoryTableCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Category-Wise Spending Breakdown & Percentage Share");
        title.setFont(UIUtil.FONT_SUBTITLE);
        title.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = { "Category Name", "Total Amount Spent", "Percentage of Total Outflow" };
        categoryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        categoryTable = new JTable(categoryTableModel);
        UIUtil.styleTable(categoryTable);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        right.setFont(UIUtil.FONT_REGULAR_BOLD);
        right.setBorder(new EmptyBorder(0, 8, 0, 10));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        center.setFont(UIUtil.FONT_REGULAR_BOLD);

        categoryTable.getColumnModel().getColumn(1).setCellRenderer(right);
        categoryTable.getColumnModel().getColumn(2).setCellRenderer(center);

        JScrollPane sp = new JScrollPane(categoryTable);
        sp.setBorder(new LineBorder(UIUtil.COLOR_BORDER, 1));
        sp.setPreferredSize(new Dimension(500, 180));
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTrendRow() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new BorderLayout());
        trendChart = new TrendLineChartPanel();
        card.add(trendChart, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        LocalDate start;
        LocalDate end;
        int year;
        int month;

        if (rbMonthly.isSelected()) {
            year = cbYear.getSelectedItem() != null ? (Integer) cbYear.getSelectedItem() : YearMonth.now().getYear();
            month = cbMonth.getSelectedIndex() + 1;
            start = DateUtil.getStartOfMonth(year, month);
            end = DateUtil.getEndOfMonth(year, month);
        } else {
            try {
                start = DateUtil.parse(tfStartDate.getText());
                end = DateUtil.parse(tfEndDate.getText());
                year = start.getYear();
                month = start.getMonthValue();
            } catch (Exception ex) {
                UIUtil.showError(this, "Invalid date format in Custom Range. Using current month.");
                year = YearMonth.now().getYear();
                month = YearMonth.now().getMonthValue();
                start = DateUtil.getStartOfMonth(year, month);
                end = DateUtil.getEndOfMonth(year, month);
            }
        }

        // Calculate Totals for Range
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : controller.getTransactionService().getAllTransactions()) {
            if (DateUtil.isDateInRange(t.getDate(), start, end)) {
                if (t.isIncome()) totalIncome = totalIncome.add(t.getAmount());
                else totalExpense = totalExpense.add(t.getAmount());
            }
        }

        BigDecimal netSavings = totalIncome.subtract(totalExpense);
        double savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0 ?
                Math.round((netSavings.doubleValue() / totalIncome.doubleValue()) * 10000.0) / 100.0 : 0.0;

        cardTotalIncome.setValue(CurrencyUtil.format(totalIncome));
        cardTotalExpense.setValue(CurrencyUtil.format(totalExpense));
        cardNetSavings.setValue(CurrencyUtil.format(netSavings));
        cardSavingsRate.setValue(savingsRate + "%");

        // Charts
        Map<String, BigDecimal> catExpenses = controller.getReportService().getExpenseCategoryBreakdown(
                start, end, controller.getTransactionService());
        pieChart.setData(catExpenses, "Period Outflow");

        String periodLabel = rbMonthly.isSelected() ? DateUtil.formatMonthYear(year, month) : (start + " to " + end);
        barChart.setComparisonData(totalIncome, totalExpense, "Period Inflow vs Outflow (" + periodLabel + ")");

        List<ReportService.MonthlyTrendPoint> trend = controller.getReportService().getYearlyTrend(
                year, controller.getTransactionService());
        trendChart.setTrendData(trend, year + " Annual Trajectory");

        // Category Breakdown Table
        categoryTableModel.setRowCount(0);
        for (Map.Entry<String, BigDecimal> entry : catExpenses.entrySet()) {
            double share = totalExpense.compareTo(BigDecimal.ZERO) > 0 ?
                    (entry.getValue().doubleValue() / totalExpense.doubleValue()) * 100.0 : 0.0;
            categoryTableModel.addRow(new Object[] {
                    entry.getKey(),
                    CurrencyUtil.format(entry.getValue()),
                    String.format("%.2f%%", share)
            });
        }
    }

    private void onExportReport() {
        int year = (Integer) cbYear.getSelectedItem();
        int month = cbMonth.getSelectedIndex() + 1;

        FinancialSummary summary = controller.getReportService().getMonthlySummary(
                year, month, controller.getTransactionService(), controller.getBudgetService());

        List<BudgetStatus> budgetStatuses = controller.getBudgetService().getCategoryBudgetStatuses(
                year, month, controller.getTransactionService());

        Map<String, BigDecimal> catExpenses = controller.getReportService().getExpenseCategoryBreakdown(
                DateUtil.getStartOfMonth(year, month), DateUtil.getEndOfMonth(year, month), controller.getTransactionService());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Financial Report to CSV");
        fileChooser.setSelectedFile(new File("Financial_Report_" + year + "_" + String.format("%02d", month) + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try {
                controller.getExportService().exportMonthlyReportToCsv(
                        year, month, summary, budgetStatuses, catExpenses, fileToSave);
                UIUtil.showSuccess(this, "Report exported successfully to:\n" + fileToSave.getAbsolutePath());
            } catch (StorageException ex) {
                UIUtil.showError(this, "Failed to export report: " + ex.getMessage());
            }
        }
    }
}
