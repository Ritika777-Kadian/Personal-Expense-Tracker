package expense.view;

import expense.controller.ExpenseController;
import expense.exception.StorageException;
import expense.model.BudgetStatus;
import expense.model.FinancialSummary;
import expense.model.Transaction;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;
import expense.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Screen dedicated to data export operations in CSV formats suitable for Microsoft Excel.
 */
public class ExportPanel extends JPanel {

    private final ExpenseController controller;

    private JComboBox<String> cbExportMonth;
    private JComboBox<Integer> cbExportYear;

    public ExportPanel(ExpenseController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtil.COLOR_MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
    }

    private void initUI() {
        // TOP Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Data Export Center");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        header.add(lblTitle);

        JLabel lblSub = new JLabel("Export your transaction logs and financial analytics into standard CSV files for Microsoft Excel");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        header.add(lblSub);

        add(header, BorderLayout.NORTH);

        // Grid of Export Cards
        JPanel grid = new JPanel(new GridLayout(3, 1, 0, 16));
        grid.setOpaque(false);

        // Card 1: Export All Transactions
        grid.add(createExportOptionCard(
                "Export Complete Transaction Ledger",
                "Exports every income and expense transaction recorded in the system with full metadata.",
                "Export All Transactions (CSV)",
                e -> exportAllTransactions()
        ));

        // Card 2: Export Monthly Financial Report
        JPanel monthCard = UIUtil.createCardPanel();
        monthCard.setLayout(new BorderLayout(16, 12));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        JLabel t2 = new JLabel("Export Monthly Financial & Budget Report");
        t2.setFont(UIUtil.FONT_SUBTITLE);
        t2.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        left.add(t2);

        JLabel d2 = new JLabel("Generates a structured report with summary metrics, category breakdown, and budget utilization.");
        d2.setFont(UIUtil.FONT_REGULAR);
        d2.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        left.add(d2);
        monthCard.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);

        String[] months = new String[] {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        cbExportMonth = UIUtil.createStyledComboBox(months);
        cbExportMonth.setSelectedIndex(YearMonth.now().getMonthValue() - 1);
        right.add(cbExportMonth);

        Integer[] years = new Integer[] { 2023, 2024, 2025, 2026, 2027 };
        cbExportYear = UIUtil.createStyledComboBox(years);
        cbExportYear.setSelectedItem(YearMonth.now().getYear());
        right.add(cbExportYear);

        JButton btnExportMonth = UIUtil.createPrimaryButton("Export Monthly Report (CSV)");
        btnExportMonth.addActionListener(e -> exportMonthlyReport());
        right.add(btnExportMonth);

        monthCard.add(right, BorderLayout.EAST);
        grid.add(monthCard);

        // Card 3: Export Formatted Guidelines / Excel note
        JPanel infoCard = UIUtil.createCardPanel();
        infoCard.setLayout(new GridLayout(3, 1, 0, 4));

        JLabel infoTitle = new JLabel("Microsoft Excel Compatibility & File Integrity");
        infoTitle.setFont(UIUtil.FONT_SUBTITLE);
        infoTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        infoCard.add(infoTitle);

        JLabel infoP1 = new JLabel("• All exported CSV files include a UTF-8 Byte Order Mark (BOM) so Excel renders currency signs and accents properly.");
        infoP1.setFont(UIUtil.FONT_SMALL);
        infoP1.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        infoCard.add(infoP1);

        JLabel infoP2 = new JLabel("• Text descriptions containing commas, quotes, or line breaks are strictly RFC-4180 escaped.");
        infoP2.setFont(UIUtil.FONT_SMALL);
        infoP2.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        infoCard.add(infoP2);

        grid.add(infoCard);

        add(grid, BorderLayout.CENTER);
    }

    private JPanel createExportOptionCard(String title, String desc, String btnText, java.awt.event.ActionListener action) {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new BorderLayout(16, 12));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIUtil.FONT_SUBTITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        left.add(lblTitle);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(UIUtil.FONT_REGULAR);
        lblDesc.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        left.add(lblDesc);

        card.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);
        JButton btn = UIUtil.createPrimaryButton(btnText);
        btn.addActionListener(action);
        right.add(btn);

        card.add(right, BorderLayout.EAST);
        return card;
    }

    private void exportAllTransactions() {
        List<Transaction> list = controller.getTransactionService().getAllTransactions();
        if (list.isEmpty()) {
            UIUtil.showError(this, "No transactions available to export.");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export All Transactions");
        fc.setSelectedFile(new File("All_Transactions_" + LocalDate.now() + ".csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = fc.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getParentFile(), target.getName() + ".csv");
            }
            try {
                controller.getExportService().exportTransactionsToCsv(list, target);
                UIUtil.showSuccess(this, "Successfully exported " + list.size() + " transactions to:\n" + target.getAbsolutePath());
            } catch (StorageException ex) {
                UIUtil.showError(this, "Export failed: " + ex.getMessage());
            }
        }
    }

    private void exportMonthlyReport() {
        int year = (Integer) cbExportYear.getSelectedItem();
        int month = cbExportMonth.getSelectedIndex() + 1;

        FinancialSummary summary = controller.getReportService().getMonthlySummary(
                year, month, controller.getTransactionService(), controller.getBudgetService());

        List<BudgetStatus> budgetStatuses = controller.getBudgetService().getCategoryBudgetStatuses(
                year, month, controller.getTransactionService());

        Map<String, BigDecimal> catExpenses = controller.getReportService().getExpenseCategoryBreakdown(
                DateUtil.getStartOfMonth(year, month), DateUtil.getEndOfMonth(year, month), controller.getTransactionService());

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Monthly Report");
        fc.setSelectedFile(new File("Monthly_Report_" + year + "_" + String.format("%02d", month) + ".csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = fc.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getParentFile(), target.getName() + ".csv");
            }
            try {
                controller.getExportService().exportMonthlyReportToCsv(
                        year, month, summary, budgetStatuses, catExpenses, target);
                UIUtil.showSuccess(this, "Monthly report exported successfully to:\n" + target.getAbsolutePath());
            } catch (StorageException ex) {
                UIUtil.showError(this, "Export failed: " + ex.getMessage());
            }
        }
    }
}
