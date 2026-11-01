package expense.view;

import expense.controller.ExpenseController;
import expense.exception.StorageException;
import expense.model.AppSettings;
import expense.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Screen for managing application preferences, active currency,
 * backup and restore, and safe data reset.
 */
public class SettingsPanel extends JPanel {

    private final ExpenseController controller;

    private JComboBox<String> cbCurrency;
    private JComboBox<String> cbTheme;
    private JLabel lblDataPath;
    private JLabel lblLastBackup;
    private JLabel lblTxnFileSize;
    private JLabel lblDemoStatus;
    private JButton btnRestoreDemo;
    private JButton btnRemoveDemo;

    public SettingsPanel(ExpenseController controller) {
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

        JLabel lblTitle = new JLabel("System Settings & Data Management");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        header.add(lblTitle);

        JLabel lblSub = new JLabel("Configure currency, review file storage status, create ZIP archives, or restore backups");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        header.add(lblSub);

        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // 1. Regional Preferences Card
        content.add(createPreferencesCard());
        content.add(Box.createVerticalStrut(16));

        // 2. Sample Demo Data Management Card
        content.add(createSampleDataCard());
        content.add(Box.createVerticalStrut(16));

        // 3. Storage & Backup Card
        content.add(createStorageCard());
        content.add(Box.createVerticalStrut(16));

        // 4. Danger Zone Card (Reset)
        content.add(createDangerZoneCard());

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        add(sp, BorderLayout.CENTER);
    }

    private JPanel createPreferencesCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        JLabel title = new JLabel("Regional & Display Preferences");
        title.setFont(UIUtil.FONT_SUBTITLE);
        title.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        card.add(title, gbc);

        // Currency
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel lblCur = new JLabel("Active Currency:");
        lblCur.setFont(UIUtil.FONT_REGULAR_BOLD);
        card.add(lblCur, gbc);

        gbc.gridx = 1;
        String[] currencies = new String[] {
                "₹ - Indian Rupee (INR)",
                "$ - US Dollar (USD)",
                "€ - Euro (EUR)",
                "£ - British Pound (GBP)",
                "¥ - Japanese Yen (JPY)",
                "$ - Canadian Dollar (CAD)",
                "$ - Australian Dollar (AUD)"
        };
        cbCurrency = UIUtil.createStyledComboBox(currencies);
        card.add(cbCurrency, gbc);

        gbc.gridx = 2;
        JButton btnSaveCur = UIUtil.createPrimaryButton("Save Currency");
        btnSaveCur.addActionListener(e -> onSaveCurrency());
        card.add(btnSaveCur, gbc);

        // Theme
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblTh = new JLabel("UI Theme:");
        lblTh.setFont(UIUtil.FONT_REGULAR_BOLD);
        card.add(lblTh, gbc);

        gbc.gridx = 1;
        cbTheme = UIUtil.createStyledComboBox(new String[] { "Modern Light (Default)", "Slate Blue" });
        card.add(cbTheme, gbc);

        gbc.gridx = 2;
        JButton btnApplyTheme = UIUtil.createSecondaryButton("Apply Theme");
        btnApplyTheme.addActionListener(e -> UIUtil.showSuccess(this, "Modern Light theme is active."));
        card.add(btnApplyTheme, gbc);

        return card;
    }

    private JPanel createSampleDataCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new BorderLayout(16, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Sample Demo Data Management");
        title.setFont(UIUtil.FONT_SUBTITLE);
        title.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(2, 1, 0, 4));
        details.setOpaque(false);

        lblDemoStatus = new JLabel("Status: Checking demo data status...");
        lblDemoStatus.setFont(UIUtil.FONT_REGULAR_BOLD);
        details.add(lblDemoStatus);

        JLabel desc = new JLabel("Load 20 realistic sample transactions and budgets for viva evaluation, or remove all demo records to start fresh.");
        desc.setFont(UIUtil.FONT_SMALL);
        desc.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        details.add(desc);

        card.add(details, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        btnRestoreDemo = UIUtil.createPrimaryButton("Restore / Load Sample Data");
        btnRestoreDemo.addActionListener(e -> onRestoreSampleData());
        actions.add(btnRestoreDemo);

        btnRemoveDemo = UIUtil.createDangerButton("Remove All Sample Data");
        btnRemoveDemo.addActionListener(e -> onRemoveSampleData());
        actions.add(btnRemoveDemo);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void onRestoreSampleData() {
        int nonDemoCount = 0;
        for (expense.model.Transaction t : controller.getTransactionService().getAllTransactions()) {
            if (!t.isDemoData()) nonDemoCount++;
        }

        if (nonDemoCount > 0) {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "You currently have " + nonDemoCount + " personal transaction(s) recorded in your ledger.\n\n" +
                    "To prevent mixing demo and personal data without permission, choose an option:\n" +
                    "• 'Replace': Clears personal transactions and loads clean sample data.\n" +
                    "• 'Append': Keeps personal records and appends the 20 sample demo records.\n" +
                    "• 'Cancel': Keeps everything unchanged.",
                    "Sample Data Loading Permission",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[] { "Append Demo Data", "Replace with Demo Data", "Cancel" },
                    "Append Demo Data"
            );

            if (choice == 1) { // Replace
                try {
                    controller.getFileManager().getTransactionStorage().saveAll(new java.util.ArrayList<>());
                    controller.getTransactionService().reload();
                    controller.loadSampleData();
                    refreshData();
                    UIUtil.showSuccess(this, "Replaced ledger with 20 realistic sample demo transactions and budgets.");
                } catch (Exception ex) {
                    UIUtil.showError(this, "Failed to load sample data: " + ex.getMessage());
                }
            } else if (choice == 0) { // Append
                try {
                    controller.loadSampleData();
                    refreshData();
                    UIUtil.showSuccess(this, "Sample demo data loaded successfully alongside existing records.");
                } catch (Exception ex) {
                    UIUtil.showError(this, "Failed to load sample data: " + ex.getMessage());
                }
            }
        } else {
            boolean confirm = UIUtil.showConfirm(this,
                    "Load 20 realistic sample transactions and monthly budgets for preview?",
                    "Confirm Load Sample Data");
            if (confirm) {
                try {
                    controller.loadSampleData();
                    refreshData();
                    UIUtil.showSuccess(this, "Sample data loaded successfully into transactions.csv and budgets.csv!");
                } catch (Exception ex) {
                    UIUtil.showError(this, "Failed to load sample data: " + ex.getMessage());
                }
            }
        }
    }

    private void onRemoveSampleData() {
        if (!controller.hasSampleData()) {
            UIUtil.showError(this, "No sample demo data found in the ledger.");
            return;
        }

        boolean confirm = UIUtil.showConfirm(this,
                "Remove all records labeled [DEMO DATA]?\n" +
                "Any transactions you created personally will NOT be deleted.",
                "Confirm Remove Sample Data");
        if (confirm) {
            try {
                int count = controller.removeSampleData();
                refreshData();
                UIUtil.showSuccess(this, "Successfully removed " + count + " sample demo records.\nYour personal ledger is clean.");
            } catch (StorageException ex) {
                UIUtil.showError(this, "Failed to remove sample data: " + ex.getMessage());
            }
        }
    }

    private JPanel createStorageCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new BorderLayout(16, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Data Folder & Backup Operations");
        title.setFont(UIUtil.FONT_SUBTITLE);
        title.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(3, 1, 0, 6));
        details.setOpaque(false);

        lblDataPath = new JLabel("Data Folder: " + controller.getFileManager().getDataDir().getAbsolutePath());
        lblDataPath.setFont(UIUtil.FONT_SMALL);
        lblDataPath.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        details.add(lblDataPath);

        lblTxnFileSize = new JLabel("Persistence Storage: transactions.csv, categories.csv, budgets.csv, settings.txt");
        lblTxnFileSize.setFont(UIUtil.FONT_SMALL);
        lblTxnFileSize.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        details.add(lblTxnFileSize);

        lblLastBackup = new JLabel("Last Backup: " + controller.getSettings().getLastBackupDate());
        lblLastBackup.setFont(UIUtil.FONT_SMALL);
        lblLastBackup.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        details.add(lblLastBackup);

        card.add(details, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnBackup = UIUtil.createPrimaryButton("Create Backup ZIP");
        btnBackup.addActionListener(e -> onBackup());
        actions.add(btnBackup);

        JButton btnRestore = UIUtil.createSecondaryButton("Restore from ZIP");
        btnRestore.addActionListener(e -> onRestore());
        actions.add(btnRestore);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createDangerZoneCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new BorderLayout(16, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 202, 202), 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel title = new JLabel("Danger Zone: Reset Application Data");
        title.setFont(UIUtil.FONT_SUBTITLE);
        title.setForeground(UIUtil.COLOR_EXPENSE);
        card.add(title, BorderLayout.NORTH);

        JLabel desc = new JLabel("This action permanently erases all transactions and custom budgets, resetting categories to default values.");
        desc.setFont(UIUtil.FONT_REGULAR);
        desc.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        card.add(desc, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);

        JButton btnReset = UIUtil.createDangerButton("Reset All Application Data");
        btnReset.addActionListener(e -> onResetData());
        btnPanel.add(btnReset);

        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    private void onSaveCurrency() {
        String selected = (String) cbCurrency.getSelectedItem();
        if (selected == null) return;

        AppSettings s = controller.getSettings();
        if (selected.contains("INR")) {
            s.setCurrencySymbol("₹");
            s.setCurrencyCode("INR");
        } else if (selected.contains("USD")) {
            s.setCurrencySymbol("$");
            s.setCurrencyCode("USD");
        } else if (selected.contains("EUR")) {
            s.setCurrencySymbol("€");
            s.setCurrencyCode("EUR");
        } else if (selected.contains("GBP")) {
            s.setCurrencySymbol("£");
            s.setCurrencyCode("GBP");
        } else if (selected.contains("JPY")) {
            s.setCurrencySymbol("¥");
            s.setCurrencyCode("JPY");
        } else if (selected.contains("CAD")) {
            s.setCurrencySymbol("CA$");
            s.setCurrencyCode("CAD");
        } else if (selected.contains("AUD")) {
            s.setCurrencySymbol("A$");
            s.setCurrencyCode("AUD");
        }

        try {
            controller.saveSettings(s);
            UIUtil.showSuccess(this, "Active currency set to " + s.getCurrencySymbol() + " (" + s.getCurrencyCode() + ")");
        } catch (StorageException ex) {
            UIUtil.showError(this, "Failed to save currency: " + ex.getMessage());
        }
    }

    private void onBackup() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Backup Archive");
        fc.setSelectedFile(new File("ExpenseTracker_Backup_" + java.time.LocalDate.now() + ".zip"));
        fc.setFileFilter(new FileNameExtensionFilter("ZIP Archives (*.zip)", "zip"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = fc.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".zip")) {
                target = new File(target.getParentFile(), target.getName() + ".zip");
            }
            try {
                controller.backupData(target);
                lblLastBackup.setText("Last Backup: " + controller.getSettings().getLastBackupDate());
                UIUtil.showSuccess(this, "Backup archive created successfully:\n" + target.getAbsolutePath());
            } catch (StorageException ex) {
                UIUtil.showError(this, "Backup failed: " + ex.getMessage());
            }
        }
    }

    private void onRestore() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Backup Archive to Restore");
        fc.setFileFilter(new FileNameExtensionFilter("ZIP Archives (*.zip)", "zip"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = fc.getSelectedFile();
            boolean confirm = UIUtil.showConfirm(this,
                    "Restoring will overwrite current ledger files with the contents of:\n" + target.getName() +
                            "\nAre you sure you want to proceed?", "Confirm Restore");

            if (confirm) {
                try {
                    controller.restoreData(target);
                    refreshData();
                    UIUtil.showSuccess(this, "Data successfully restored from backup!");
                } catch (StorageException ex) {
                    UIUtil.showError(this, "Restore failed: " + ex.getMessage());
                }
            }
        }
    }

    private void onResetData() {
        String input = JOptionPane.showInputDialog(this,
                "WARNING: This will permanently delete all transactions and budgets!\nType 'RESET' to confirm:",
                "Danger Zone Confirmation", JOptionPane.WARNING_MESSAGE);

        if ("RESET".equalsIgnoreCase(input != null ? input.trim() : "")) {
            try {
                controller.resetAllData();
                refreshData();
                UIUtil.showSuccess(this, "Application data has been reset to initial empty defaults.");
            } catch (StorageException ex) {
                UIUtil.showError(this, "Reset failed: " + ex.getMessage());
            }
        } else if (input != null) {
            UIUtil.showError(this, "Reset cancelled. You did not type 'RESET'.");
        }
    }

    public void refreshData() {
        AppSettings s = controller.getSettings();
        String sym = s.getCurrencySymbol();
        if ("₹".equals(sym)) cbCurrency.setSelectedIndex(0);
        else if ("$".equals(sym) && "USD".equals(s.getCurrencyCode())) cbCurrency.setSelectedIndex(1);
        else if ("€".equals(sym)) cbCurrency.setSelectedIndex(2);
        else if ("£".equals(sym)) cbCurrency.setSelectedIndex(3);
        else if ("¥".equals(sym)) cbCurrency.setSelectedIndex(4);

        lblLastBackup.setText("Last Backup: " + s.getLastBackupDate());

        // Update Demo Data status
        if (lblDemoStatus != null) {
            boolean hasDemo = controller.hasSampleData();
            int count = controller.getSampleDataCount();
            if (hasDemo) {
                lblDemoStatus.setText("Status: DEMO DATA ACTIVE (" + count + " sample transactions loaded)");
                lblDemoStatus.setForeground(UIUtil.COLOR_WARNING);
            } else {
                lblDemoStatus.setText("Status: Clean Personal Ledger (No demo data present)");
                lblDemoStatus.setForeground(UIUtil.COLOR_INCOME);
            }
            if (btnRemoveDemo != null) {
                btnRemoveDemo.setEnabled(hasDemo);
            }
        }
    }
}
