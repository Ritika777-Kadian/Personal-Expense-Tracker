package expense.view;

import expense.controller.ExpenseController;
import expense.exception.EntityNotFoundException;
import expense.exception.StorageException;
import expense.exception.ValidationException;
import expense.model.Transaction;
import expense.model.TransactionType;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;
import expense.util.UIUtil;
import expense.view.component.TransactionTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Screen for viewing, sorting, editing, and deleting financial transactions.
 */
public class TransactionPanel extends JPanel {

    private final ExpenseController controller;
    private final Consumer<String> navigationHandler;

    private JTable table;
    private TransactionTableModel tableModel;
    private TableRowSorter<TransactionTableModel> sorter;

    private JTextField tfSearch;
    private JComboBox<String> cbTypeFilter;
    private JComboBox<String> cbCategoryFilter;
    private JLabel lblStatusCount;
    private JLabel lblTotalSum;

    public TransactionPanel(ExpenseController controller, Consumer<String> navigationHandler) {
        this.controller = controller;
        this.navigationHandler = navigationHandler;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtil.COLOR_MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // TOP: Header with Action Buttons
        add(createHeaderPanel(), BorderLayout.NORTH);

        // CENTER: Filter Bar + Table inside a clean Card
        JPanel centerCard = UIUtil.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 12));

        centerCard.add(createFilterBar(), BorderLayout.NORTH);

        tableModel = new TransactionTableModel();
        table = new JTable(tableModel);
        UIUtil.styleTable(table);
        TransactionTableModel.applyRenderers(table);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditSelected();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UIUtil.COLOR_BORDER, 1));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Table Info Bar (Count & Net Total)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 4, 4, 4));

        lblStatusCount = new JLabel("Showing 0 transactions");
        lblStatusCount.setFont(UIUtil.FONT_REGULAR_BOLD);
        lblStatusCount.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        footer.add(lblStatusCount, BorderLayout.WEST);

        lblTotalSum = new JLabel("Net: ₹ 0.00");
        lblTotalSum.setFont(UIUtil.FONT_REGULAR_BOLD);
        lblTotalSum.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        footer.add(lblTotalSum, BorderLayout.EAST);

        centerCard.add(footer, BorderLayout.SOUTH);

        add(centerCard, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Transaction History");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        titlePanel.add(lblTitle);

        JLabel lblSub = new JLabel("Browse, filter, edit, or delete existing transaction records");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        titlePanel.add(lblSub);

        header.add(titlePanel, BorderLayout.WEST);

        // Action Buttons: Add, Edit, Delete
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnAdd = UIUtil.createPrimaryButton("+ Add New Transaction");
        btnAdd.addActionListener(e -> {
            if (navigationHandler != null) navigationHandler.accept("ADD_TRANSACTION");
        });
        btnPanel.add(btnAdd);

        JButton btnEdit = UIUtil.createSecondaryButton("Edit Selected");
        btnEdit.addActionListener(e -> onEditSelected());
        btnPanel.add(btnEdit);

        JButton btnDelete = UIUtil.createDangerButton("Delete Selected");
        btnDelete.addActionListener(e -> onDeleteSelected());
        btnPanel.add(btnDelete);

        header.add(btnPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        bar.setOpaque(false);

        // Search Field
        bar.add(new JLabel("Search:"));
        tfSearch = UIUtil.createStyledTextField(14);
        tfSearch.setToolTipText("Search description, category, or ID");
        tfSearch.addActionListener(e -> applyFilter());
        bar.add(tfSearch);

        // Type Filter
        bar.add(new JLabel("Type:"));
        cbTypeFilter = UIUtil.createStyledComboBox(new String[] { "All Types", "Income", "Expense" });
        cbTypeFilter.addActionListener(e -> applyFilter());
        bar.add(cbTypeFilter);

        // Category Filter
        bar.add(new JLabel("Category:"));
        cbCategoryFilter = UIUtil.createStyledComboBox(new String[] { "All Categories" });
        cbCategoryFilter.addActionListener(e -> applyFilter());
        bar.add(cbCategoryFilter);

        JButton btnFilter = UIUtil.createSecondaryButton("Apply");
        btnFilter.addActionListener(e -> applyFilter());
        bar.add(btnFilter);

        JButton btnReset = UIUtil.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> resetFilter());
        bar.add(btnReset);

        return bar;
    }

    private void updateCategoryDropdown() {
        cbCategoryFilter.removeAllItems();
        cbCategoryFilter.addItem("All Categories");
        for (expense.model.Category c : controller.getCategoryService().getAllCategories()) {
            cbCategoryFilter.addItem(c.getName());
        }
    }

    private void applyFilter() {
        String keyword = tfSearch.getText().trim();
        String selectedType = (String) cbTypeFilter.getSelectedItem();
        TransactionType type = null;
        if ("Income".equalsIgnoreCase(selectedType)) type = TransactionType.INCOME;
        else if ("Expense".equalsIgnoreCase(selectedType)) type = TransactionType.EXPENSE;

        String selectedCat = (String) cbCategoryFilter.getSelectedItem();

        List<Transaction> filtered = controller.getTransactionService().filterTransactions(
                type, selectedCat, null, null, null, null, keyword);

        tableModel.setTransactions(filtered);
        updateFooterMetrics(filtered);
    }

    private void resetFilter() {
        tfSearch.setText("");
        cbTypeFilter.setSelectedIndex(0);
        cbCategoryFilter.setSelectedIndex(0);
        refreshData();
    }

    private void updateFooterMetrics(List<Transaction> list) {
        lblStatusCount.setText("Showing " + list.size() + " transactions");
        BigDecimal net = BigDecimal.ZERO;
        for (Transaction t : list) {
            if (t.isIncome()) {
                net = net.add(t.getAmount());
            } else {
                net = net.subtract(t.getAmount());
            }
        }
        lblTotalSum.setText("Net Balance for View: " + CurrencyUtil.format(net));
    }

    private void onEditSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            UIUtil.showError(this, "Please select a transaction to edit.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Transaction txn = tableModel.getTransactionAt(modelRow);
        if (txn == null) return;

        // Open Edit Dialog
        showEditDialog(txn);
    }

    private void showEditDialog(Transaction txn) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Transaction - " + txn.getId(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(440, 420);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Type
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        JComboBox<TransactionType> cbType = UIUtil.createStyledComboBox(TransactionType.values());
        cbType.setSelectedItem(txn.getType());
        form.add(cbType, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cbCat = new JComboBox<>();
        UIUtil.createStyledComboBox(new String[0]);
        updateDialogCategories(cbCat, (TransactionType) cbType.getSelectedItem(), txn.getCategory());
        cbType.addActionListener(e -> updateDialogCategories(cbCat, (TransactionType) cbType.getSelectedItem(), null));
        form.add(cbCat, gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Amount (" + CurrencyUtil.getActiveSymbol() + "):"), gbc);
        gbc.gridx = 1;
        JTextField tfAmount = UIUtil.createStyledTextField(12);
        tfAmount.setText(txn.getAmount().toPlainString());
        form.add(tfAmount, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextField tfDesc = UIUtil.createStyledTextField(12);
        tfDesc.setText(txn.getDescription());
        form.add(tfDesc, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        JTextField tfDate = UIUtil.createStyledTextField(12);
        tfDate.setText(txn.getDate().toString());
        form.add(tfDate, gbc);

        dialog.add(form, BorderLayout.CENTER);

        // Dialog Buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnBar.setBackground(new Color(248, 250, 252));
        btnBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtil.COLOR_BORDER));

        JButton btnCancel = UIUtil.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnBar.add(btnCancel);

        JButton btnSave = UIUtil.createPrimaryButton("Save Changes");
        btnSave.addActionListener(e -> {
            try {
                TransactionType type = (TransactionType) cbType.getSelectedItem();
                String cat = (String) cbCat.getSelectedItem();
                BigDecimal amount = CurrencyUtil.parseAmount(tfAmount.getText());
                String desc = tfDesc.getText().trim();
                LocalDate date = DateUtil.parse(tfDate.getText());

                txn.setType(type);
                txn.setCategory(cat);
                txn.setAmount(amount);
                txn.setDescription(desc);
                txn.setDate(date);

                controller.getTransactionService().updateTransaction(txn);
                controller.notifyDataChanged();
                dialog.dispose();
                UIUtil.showSuccess(this, "Transaction updated successfully!");
            } catch (ValidationException | IllegalArgumentException ex) {
                UIUtil.showError(dialog, ex.getMessage());
            } catch (EntityNotFoundException | StorageException ex) {
                UIUtil.showError(dialog, "Error updating transaction: " + ex.getMessage());
            }
        });
        btnBar.add(btnSave);

        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateDialogCategories(JComboBox<String> cbCat, TransactionType type, String currentCat) {
        cbCat.removeAllItems();
        List<String> names = controller.getCategoryService().getCategoryNamesByType(type);
        for (String n : names) {
            cbCat.addItem(n);
        }
        if (currentCat != null && names.contains(currentCat)) {
            cbCat.setSelectedItem(currentCat);
        }
    }

    private void onDeleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            UIUtil.showError(this, "Please select a transaction to delete.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Transaction txn = tableModel.getTransactionAt(modelRow);
        if (txn == null) return;

        boolean confirm = UIUtil.showConfirm(this,
                "Are you sure you want to delete transaction " + txn.getId() + " (" +
                        CurrencyUtil.format(txn.getAmount()) + " - " + txn.getCategory() + ")?\nThis action cannot be undone.",
                "Confirm Deletion");

        if (confirm) {
            try {
                controller.getTransactionService().deleteTransaction(txn.getId());
                controller.notifyDataChanged();
                UIUtil.showSuccess(this, "Transaction deleted successfully.");
            } catch (EntityNotFoundException | StorageException ex) {
                UIUtil.showError(this, "Failed to delete transaction: " + ex.getMessage());
            }
        }
    }

    public void refreshData() {
        updateCategoryDropdown();
        List<Transaction> list = controller.getTransactionService().getAllTransactions();
        tableModel.setTransactions(list);
        updateFooterMetrics(list);
    }
}
