package expense.view;

import expense.controller.ExpenseController;
import expense.exception.DuplicateEntityException;
import expense.exception.EntityNotFoundException;
import expense.exception.StorageException;
import expense.exception.ValidationException;
import expense.model.Category;
import expense.model.TransactionType;
import expense.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Screen for managing and customizing Income and Expense Categories.
 */
public class CategoryPanel extends JPanel {

    private final ExpenseController controller;

    private DefaultTableModel expenseTableModel;
    private DefaultTableModel incomeTableModel;
    private JTable expenseTable;
    private JTable incomeTable;

    private JTextField tfNewName;
    private JComboBox<TransactionType> cbNewType;

    public CategoryPanel(ExpenseController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtil.COLOR_MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // TOP Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Category Management");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        header.add(lblTitle);

        JLabel lblSub = new JLabel("Organize expenses and income streams. Built-in protection prevents deleting active categories.");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        header.add(lblSub);

        add(header, BorderLayout.NORTH);

        // CENTER: Dual Tables + Add Form
        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);

        // Add Category Card at the top of content
        mainContent.add(createAddCategoryCard(), BorderLayout.NORTH);

        // Dual Table Card (Tabs for Expense Categories and Income Categories)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtil.FONT_REGULAR_BOLD);

        // 1. Expense Tab
        tabbedPane.addTab("Expense Categories", createCategoryTablePanel(TransactionType.EXPENSE));

        // 2. Income Tab
        tabbedPane.addTab("Income Categories", createCategoryTablePanel(TransactionType.INCOME));

        mainContent.add(tabbedPane, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createAddCategoryCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 8));

        card.add(new JLabel("New Category:"));
        tfNewName = UIUtil.createStyledTextField(14);
        tfNewName.setToolTipText("Enter category name (e.g. Subscriptions)");
        card.add(tfNewName);

        card.add(new JLabel("Type:"));
        cbNewType = UIUtil.createStyledComboBox(TransactionType.values());
        card.add(cbNewType);

        JButton btnAdd = UIUtil.createPrimaryButton("+ Add Category");
        btnAdd.addActionListener(e -> onAddCategory());
        card.add(btnAdd);

        return card;
    }

    private JPanel createCategoryTablePanel(TransactionType type) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = { "Category Name", "Classification", "Nature", "Transactions Count" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        UIUtil.styleTable(table);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        if (type == TransactionType.EXPENSE) {
            expenseTableModel = model;
            expenseTable = table;
        } else {
            incomeTableModel = model;
            incomeTable = table;
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(UIUtil.COLOR_BORDER, 1));
        panel.add(sp, BorderLayout.CENTER);

        // Actions: Rename, Delete
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        btnBar.setOpaque(false);

        JButton btnRename = UIUtil.createSecondaryButton("Rename Selected");
        btnRename.addActionListener(e -> onRenameCategory(table, type));
        btnBar.add(btnRename);

        JButton btnDelete = UIUtil.createDangerButton("Delete Selected");
        btnDelete.addActionListener(e -> onDeleteCategory(table, type));
        btnBar.add(btnDelete);

        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
    }

    private void onAddCategory() {
        String name = tfNewName.getText().trim();
        TransactionType type = (TransactionType) cbNewType.getSelectedItem();

        try {
            controller.getCategoryService().addCategory(name, type);
            controller.notifyDataChanged();
            tfNewName.setText("");
            UIUtil.showSuccess(this, "Category '" + name + "' added successfully!");
        } catch (ValidationException | DuplicateEntityException ex) {
            UIUtil.showError(this, ex.getMessage());
        } catch (StorageException ex) {
            UIUtil.showError(this, "Storage error: " + ex.getMessage());
        }
    }

    private void onRenameCategory(JTable table, TransactionType type) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.showError(this, "Please select a category to rename.");
            return;
        }

        String currentName = (String) table.getValueAt(row, 0);
        String newName = JOptionPane.showInputDialog(this, 
                "Enter new name for category '" + currentName + "':", currentName);

        if (newName != null && !newName.trim().isEmpty() && !newName.trim().equalsIgnoreCase(currentName)) {
            try {
                controller.getCategoryService().renameCategory(
                        currentName, newName.trim(), type, controller.getTransactionService(), controller.getBudgetService());
                controller.notifyDataChanged();
                UIUtil.showSuccess(this, "Category renamed to '" + newName.trim() + "'. Existing transactions updated.");
            } catch (ValidationException | DuplicateEntityException | EntityNotFoundException ex) {
                UIUtil.showError(this, ex.getMessage());
            } catch (StorageException ex) {
                UIUtil.showError(this, "Storage error: " + ex.getMessage());
            }
        }
    }

    private void onDeleteCategory(JTable table, TransactionType type) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.showError(this, "Please select a category to delete.");
            return;
        }

        String catName = (String) table.getValueAt(row, 0);
        boolean confirm = UIUtil.showConfirm(this, 
                "Are you sure you want to delete category '" + catName + "'?", "Confirm Delete Category");

        if (confirm) {
            try {
                controller.getCategoryService().deleteCategory(catName, type, controller.getTransactionService());
                controller.notifyDataChanged();
                UIUtil.showSuccess(this, "Category '" + catName + "' deleted successfully.");
            } catch (ValidationException ex) {
                UIUtil.showError(this, ex.getMessage());
            } catch (EntityNotFoundException | StorageException ex) {
                UIUtil.showError(this, "Failed to delete: " + ex.getMessage());
            }
        }
    }

    public void refreshData() {
        populateTable(expenseTableModel, TransactionType.EXPENSE);
        populateTable(incomeTableModel, TransactionType.INCOME);
    }

    private void populateTable(DefaultTableModel model, TransactionType type) {
        if (model == null) return;
        model.setRowCount(0);
        List<Category> list = controller.getCategoryService().getCategoriesByType(type);

        for (Category c : list) {
            int txnCount = controller.getTransactionService().countTransactionsForCategory(c.getName(), type);
            model.addRow(new Object[] {
                    c.getName(),
                    c.getType().getDisplayName(),
                    c.isDefault() ? "Default System" : "User Custom",
                    txnCount + " transaction" + (txnCount == 1 ? "" : "s")
            });
        }
    }
}
