package expense.view;

import expense.controller.ExpenseController;
import expense.exception.StorageException;
import expense.model.Transaction;
import expense.model.TransactionType;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;
import expense.util.UIUtil;
import expense.view.component.TransactionTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Screen providing multi-criteria search and dynamic filtering of transactions.
 */
public class SearchPanel extends JPanel {

    private final ExpenseController controller;

    private JComboBox<String> cbType;
    private JComboBox<String> cbCategory;
    private JTextField tfKeyword;
    private JTextField tfStartDate;
    private JTextField tfEndDate;
    private JTextField tfMinAmount;
    private JTextField tfMaxAmount;

    private JTable resultsTable;
    private TransactionTableModel tableModel;
    private JLabel lblResultCount;
    private JLabel lblFilteredNet;

    public SearchPanel(ExpenseController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtil.COLOR_MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // TOP Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Advanced Search & Filtering");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        header.add(lblTitle);

        JLabel lblSub = new JLabel("Query your financial ledger by keyword, date intervals, amount bounds, and categories");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        header.add(lblSub);

        add(header, BorderLayout.NORTH);

        // Filter Criteria Card
        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setOpaque(false);

        centerContainer.add(createFilterCriteriaCard(), BorderLayout.NORTH);

        // Results Card
        JPanel resultsCard = UIUtil.createCardPanel();
        resultsCard.setLayout(new BorderLayout(0, 10));

        JPanel resultsHeader = new JPanel(new BorderLayout());
        resultsHeader.setOpaque(false);

        lblResultCount = new JLabel("Found 0 matching transactions");
        lblResultCount.setFont(UIUtil.FONT_REGULAR_BOLD);
        lblResultCount.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        resultsHeader.add(lblResultCount, BorderLayout.WEST);

        JPanel rightAct = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightAct.setOpaque(false);

        lblFilteredNet = new JLabel("Net: ₹ 0.00");
        lblFilteredNet.setFont(UIUtil.FONT_REGULAR_BOLD);
        lblFilteredNet.setForeground(UIUtil.COLOR_PRIMARY);
        rightAct.add(lblFilteredNet);

        JButton btnExportFiltered = UIUtil.createSuccessButton("Export Results to CSV");
        btnExportFiltered.addActionListener(e -> onExportResults());
        rightAct.add(btnExportFiltered);

        resultsHeader.add(rightAct, BorderLayout.EAST);
        resultsCard.add(resultsHeader, BorderLayout.NORTH);

        tableModel = new TransactionTableModel();
        resultsTable = new JTable(tableModel);
        UIUtil.styleTable(resultsTable);
        TransactionTableModel.applyRenderers(resultsTable);

        JScrollPane sp = new JScrollPane(resultsTable);
        sp.setBorder(new LineBorder(UIUtil.COLOR_BORDER, 1));
        resultsCard.add(sp, BorderLayout.CENTER);

        centerContainer.add(resultsCard, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createFilterCriteriaCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        // Row 1: Keyword, Type, Category
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(new JLabel("Keyword:"), gbc);
        gbc.gridx = 1;
        tfKeyword = UIUtil.createStyledTextField(12);
        tfKeyword.setToolTipText("Search description, note, or ID");
        card.add(tfKeyword, gbc);

        gbc.gridx = 2;
        card.add(new JLabel("Type:"), gbc);
        gbc.gridx = 3;
        cbType = UIUtil.createStyledComboBox(new String[] { "All Types", "Income", "Expense" });
        card.add(cbType, gbc);

        gbc.gridx = 4;
        card.add(new JLabel("Category:"), gbc);
        gbc.gridx = 5;
        cbCategory = UIUtil.createStyledComboBox(new String[] { "All Categories" });
        card.add(cbCategory, gbc);

        // Row 2: Start Date, End Date, Min Amount, Max Amount
        gbc.gridx = 0; gbc.gridy = 1;
        card.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        tfStartDate = UIUtil.createStyledTextField(10);
        tfStartDate.setToolTipText("yyyy-MM-dd or blank");
        card.add(tfStartDate, gbc);

        gbc.gridx = 2;
        card.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 3;
        tfEndDate = UIUtil.createStyledTextField(10);
        tfEndDate.setToolTipText("yyyy-MM-dd or blank");
        card.add(tfEndDate, gbc);

        gbc.gridx = 4;
        card.add(new JLabel("Min / Max (" + CurrencyUtil.getActiveSymbol() + "):"), gbc);
        gbc.gridx = 5;
        JPanel amtWrap = new JPanel(new GridLayout(1, 2, 4, 0));
        amtWrap.setOpaque(false);
        tfMinAmount = UIUtil.createStyledTextField(5);
        tfMinAmount.setToolTipText("Min amount");
        tfMaxAmount = UIUtil.createStyledTextField(5);
        tfMaxAmount.setToolTipText("Max amount");
        amtWrap.add(tfMinAmount);
        amtWrap.add(tfMaxAmount);
        card.add(amtWrap, gbc);

        // Row 3: Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 6;
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        btnBar.setOpaque(false);

        JButton btnClear = UIUtil.createSecondaryButton("Clear Filters");
        btnClear.addActionListener(e -> clearFilters());
        btnBar.add(btnClear);

        JButton btnSearch = UIUtil.createPrimaryButton("Search Transactions");
        btnSearch.addActionListener(e -> performSearch());
        btnBar.add(btnSearch);

        card.add(btnBar, gbc);

        return card;
    }

    private void performSearch() {
        try {
            String kw = tfKeyword.getText().trim();
            String typeStr = (String) cbType.getSelectedItem();
            TransactionType type = null;
            if ("Income".equalsIgnoreCase(typeStr)) type = TransactionType.INCOME;
            else if ("Expense".equalsIgnoreCase(typeStr)) type = TransactionType.EXPENSE;

            String cat = (String) cbCategory.getSelectedItem();

            LocalDate start = !tfStartDate.getText().isBlank() ? DateUtil.parse(tfStartDate.getText()) : null;
            LocalDate end = !tfEndDate.getText().isBlank() ? DateUtil.parse(tfEndDate.getText()) : null;

            BigDecimal min = !tfMinAmount.getText().isBlank() ? CurrencyUtil.parseAmount(tfMinAmount.getText()) : null;
            BigDecimal max = !tfMaxAmount.getText().isBlank() ? CurrencyUtil.parseAmount(tfMaxAmount.getText()) : null;

            List<Transaction> results = controller.getTransactionService().filterTransactions(
                    type, cat, start, end, min, max, kw);

            tableModel.setTransactions(results);
            lblResultCount.setText("Found " + results.size() + " matching transactions");

            BigDecimal net = BigDecimal.ZERO;
            for (Transaction t : results) {
                if (t.isIncome()) net = net.add(t.getAmount());
                else net = net.subtract(t.getAmount());
            }
            lblFilteredNet.setText("Net Balance: " + CurrencyUtil.format(net));

        } catch (Exception ex) {
            UIUtil.showError(this, "Filter error: " + ex.getMessage());
        }
    }

    private void clearFilters() {
        tfKeyword.setText("");
        cbType.setSelectedIndex(0);
        cbCategory.setSelectedIndex(0);
        tfStartDate.setText("");
        tfEndDate.setText("");
        tfMinAmount.setText("");
        tfMaxAmount.setText("");
        refreshData();
    }

    private void onExportResults() {
        List<Transaction> currentList = controller.getTransactionService().getAllTransactions();
        int rowCount = tableModel.getRowCount();
        java.util.List<Transaction> displayed = new java.util.ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            displayed.add(tableModel.getTransactionAt(i));
        }

        if (displayed.isEmpty()) {
            UIUtil.showError(this, "No transactions to export.");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Filtered Transactions to CSV");
        fc.setSelectedFile(new File("Filtered_Transactions.csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = fc.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getParentFile(), target.getName() + ".csv");
            }
            try {
                controller.getExportService().exportTransactionsToCsv(displayed, target);
                UIUtil.showSuccess(this, "Exported " + displayed.size() + " records to:\n" + target.getAbsolutePath());
            } catch (StorageException ex) {
                UIUtil.showError(this, "Export failed: " + ex.getMessage());
            }
        }
    }

    public void refreshData() {
        cbCategory.removeAllItems();
        cbCategory.addItem("All Categories");
        for (expense.model.Category c : controller.getCategoryService().getAllCategories()) {
            cbCategory.addItem(c.getName());
        }
        performSearch();
    }
}
