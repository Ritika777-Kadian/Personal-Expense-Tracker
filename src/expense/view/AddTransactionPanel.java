package expense.view;

import expense.controller.ExpenseController;
import expense.exception.StorageException;
import expense.exception.ValidationException;
import expense.model.Transaction;
import expense.model.TransactionType;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;
import expense.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dedicated screen for creating new Income or Expense transaction records.
 */
public class AddTransactionPanel extends JPanel {

    private final ExpenseController controller;
    private final Consumer<String> navigationHandler;

    private JComboBox<TransactionType> cbType;
    private JComboBox<String> cbCategory;
    private JTextField tfAmount;
    private JTextField tfDescription;
    private JTextField tfDate;
    private JLabel lblCurrencySymbol;

    public AddTransactionPanel(ExpenseController controller, Consumer<String> navigationHandler) {
        this.controller = controller;
        this.navigationHandler = navigationHandler;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtil.COLOR_MAIN_BG);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initUI();
    }

    private void initUI() {
        // Top Title
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Add New Transaction");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        header.add(lblTitle);

        JLabel lblSub = new JLabel("Record an incoming earning or an outgoing expenditure with category details");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        header.add(lblSub);

        add(header, BorderLayout.NORTH);

        // Center Form inside a styled card
        JPanel formCard = UIUtil.createCardPanel();
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 12, 10, 12);

        // 1. Transaction Type
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        JLabel lblType = new JLabel("Transaction Type *");
        lblType.setFont(UIUtil.FONT_REGULAR_BOLD);
        formCard.add(lblType, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        cbType = UIUtil.createStyledComboBox(TransactionType.values());
        cbType.addActionListener(e -> updateCategoriesForSelectedType());
        formCard.add(cbType, gbc);

        // 2. Category
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblCat = new JLabel("Category *");
        lblCat.setFont(UIUtil.FONT_REGULAR_BOLD);
        formCard.add(lblCat, gbc);

        gbc.gridx = 1;
        cbCategory = UIUtil.createStyledComboBox(new String[0]);
        formCard.add(cbCategory, gbc);

        // 3. Amount
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblAmt = new JLabel("Amount *");
        lblAmt.setFont(UIUtil.FONT_REGULAR_BOLD);
        formCard.add(lblAmt, gbc);

        gbc.gridx = 1;
        JPanel amtPanel = new JPanel(new BorderLayout(8, 0));
        amtPanel.setOpaque(false);
        lblCurrencySymbol = new JLabel(CurrencyUtil.getActiveSymbol());
        lblCurrencySymbol.setFont(UIUtil.FONT_TITLE);
        lblCurrencySymbol.setForeground(UIUtil.COLOR_PRIMARY);
        amtPanel.add(lblCurrencySymbol, BorderLayout.WEST);

        tfAmount = UIUtil.createStyledTextField(14);
        tfAmount.setToolTipText("Enter monetary amount (e.g., 250.00)");
        amtPanel.add(tfAmount, BorderLayout.CENTER);
        formCard.add(amtPanel, gbc);

        // 4. Description
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblDesc = new JLabel("Description");
        lblDesc.setFont(UIUtil.FONT_REGULAR_BOLD);
        formCard.add(lblDesc, gbc);

        gbc.gridx = 1;
        tfDescription = UIUtil.createStyledTextField(20);
        tfDescription.setToolTipText("Enter brief memo or note (e.g., Grocery shopping at supermarket)");
        formCard.add(tfDescription, gbc);

        // 5. Date
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblDate = new JLabel("Date *");
        lblDate.setFont(UIUtil.FONT_REGULAR_BOLD);
        formCard.add(lblDate, gbc);

        gbc.gridx = 1;
        JPanel datePanel = new JPanel(new BorderLayout(8, 0));
        datePanel.setOpaque(false);
        tfDate = UIUtil.createStyledTextField(12);
        tfDate.setText(LocalDate.now().toString());
        datePanel.add(tfDate, BorderLayout.CENTER);

        JButton btnToday = UIUtil.createSecondaryButton("Today");
        btnToday.addActionListener(e -> tfDate.setText(LocalDate.now().toString()));
        datePanel.add(btnToday, BorderLayout.EAST);

        formCard.add(datePanel, gbc);

        // 6. Action Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(24, 12, 10, 12);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonRow.setOpaque(false);

        JButton btnClear = UIUtil.createSecondaryButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());
        buttonRow.add(btnClear);

        JButton btnSave = UIUtil.createPrimaryButton("Save Transaction");
        btnSave.addActionListener(e -> saveTransaction());
        buttonRow.add(btnSave);

        formCard.add(buttonRow, gbc);

        // Center wrap
        JPanel outerWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        outerWrap.setOpaque(false);
        formCard.setPreferredSize(new Dimension(650, 420));
        outerWrap.add(formCard);

        add(outerWrap, BorderLayout.CENTER);

        updateCategoriesForSelectedType();
    }

    public void setPrefilledType(TransactionType type) {
        cbType.setSelectedItem(type);
        updateCategoriesForSelectedType();
        tfAmount.requestFocusInWindow();
    }

    private void updateCategoriesForSelectedType() {
        TransactionType type = (TransactionType) cbType.getSelectedItem();
        cbCategory.removeAllItems();
        if (type != null) {
            List<String> names = controller.getCategoryService().getCategoryNamesByType(type);
            for (String n : names) {
                cbCategory.addItem(n);
            }
        }
    }

    private void saveTransaction() {
        try {
            TransactionType type = (TransactionType) cbType.getSelectedItem();
            String cat = (String) cbCategory.getSelectedItem();
            BigDecimal amount = CurrencyUtil.parseAmount(tfAmount.getText());
            String desc = tfDescription.getText().trim();
            LocalDate date = DateUtil.parse(tfDate.getText());

            Transaction txn = new Transaction(type, amount, cat, desc, date);
            controller.getTransactionService().addTransaction(txn);
            controller.notifyDataChanged();

            UIUtil.showSuccess(this, "Transaction " + txn.getId() + " added successfully!");
            clearForm();

            // Option to view in transaction list
            if (navigationHandler != null) {
                navigationHandler.accept("TRANSACTIONS");
            }
        } catch (ValidationException | IllegalArgumentException ex) {
            UIUtil.showError(this, ex.getMessage());
        } catch (StorageException ex) {
            UIUtil.showError(this, "Storage Error: " + ex.getMessage());
        }
    }

    public void clearForm() {
        tfAmount.setText("");
        tfDescription.setText("");
        tfDate.setText(LocalDate.now().toString());
        lblCurrencySymbol.setText(CurrencyUtil.getActiveSymbol());
        if (cbCategory.getItemCount() > 0) {
            cbCategory.setSelectedIndex(0);
        }
    }

    public void refreshData() {
        lblCurrencySymbol.setText(CurrencyUtil.getActiveSymbol());
        updateCategoriesForSelectedType();
    }
}
