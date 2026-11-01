package expense.view;

import expense.controller.ExpenseController;
import expense.model.AppSettings;
import expense.model.TransactionType;
import expense.util.CurrencyUtil;
import expense.util.UIUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window featuring a dark sidebar, top live header,
 * and a CardLayout content area housing the 10 core pages.
 */
public class MainFrame extends JFrame implements ExpenseController.DataChangeListener {

    // Page identifier keys
    public static final String PAGE_DASHBOARD = "PAGE_DASHBOARD";
    public static final String PAGE_TRANSACTIONS = "PAGE_TRANSACTIONS";
    public static final String PAGE_ADD_TRANSACTION = "PAGE_ADD_TRANSACTION";
    public static final String PAGE_CATEGORIES = "PAGE_CATEGORIES";
    public static final String PAGE_BUDGET = "PAGE_BUDGET";
    public static final String PAGE_REPORTS = "PAGE_REPORTS";
    public static final String PAGE_SEARCH = "PAGE_SEARCH";
    public static final String PAGE_EXPORT = "PAGE_EXPORT";
    public static final String PAGE_SETTINGS = "PAGE_SETTINGS";
    public static final String PAGE_ABOUT = "PAGE_ABOUT";

    private final ExpenseController controller;

    private CardLayout cardLayout;
    private JPanel mainContentCardPanel;

    private JLabel lblCurrentClock;
    private JLabel lblHeaderSection;
    private JLabel lblCurrencyBadge;
    private JLabel lblDemoBadge;

    private final Map<String, JButton> sidebarButtons = new HashMap<>();
    private String activePageKey = PAGE_DASHBOARD;

    // References to specific panels for targeted actions
    private DashboardPanel dashboardPanel;
    private TransactionPanel transactionPanel;
    private AddTransactionPanel addTransactionPanel;
    private CategoryPanel categoryPanel;
    private BudgetPanel budgetPanel;
    private ReportsPanel reportsPanel;
    private SearchPanel searchPanel;
    private ExportPanel exportPanel;
    private SettingsPanel settingsPanel;
    private AboutPanel aboutPanel;

    public MainFrame(ExpenseController controller) {
        this.controller = controller;
        this.controller.addDataChangeListener(this);

        setTitle("Personal Expense Tracker — Java Desktop Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 840);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        initUI();
        startClockTimer();
        checkFirstLaunch();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. WEST: Navigation Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // 2. CENTER: Header + Main Pages
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(UIUtil.COLOR_MAIN_BG);

        // Top Header
        rightContainer.add(createHeaderBar(), BorderLayout.NORTH);

        // CardLayout Panel
        cardLayout = new CardLayout();
        mainContentCardPanel = new JPanel(cardLayout);
        mainContentCardPanel.setOpaque(false);

        // Initialize 10 core views
        dashboardPanel = new DashboardPanel(controller, this::handleNavigationCommand);
        transactionPanel = new TransactionPanel(controller, this::handleNavigationCommand);
        addTransactionPanel = new AddTransactionPanel(controller, this::handleNavigationCommand);
        categoryPanel = new CategoryPanel(controller);
        budgetPanel = new BudgetPanel(controller);
        reportsPanel = new ReportsPanel(controller);
        searchPanel = new SearchPanel(controller);
        exportPanel = new ExportPanel(controller);
        settingsPanel = new SettingsPanel(controller);
        aboutPanel = new AboutPanel(controller);

        mainContentCardPanel.add(dashboardPanel, PAGE_DASHBOARD);
        mainContentCardPanel.add(transactionPanel, PAGE_TRANSACTIONS);
        mainContentCardPanel.add(addTransactionPanel, PAGE_ADD_TRANSACTION);
        mainContentCardPanel.add(categoryPanel, PAGE_CATEGORIES);
        mainContentCardPanel.add(budgetPanel, PAGE_BUDGET);
        mainContentCardPanel.add(reportsPanel, PAGE_REPORTS);
        mainContentCardPanel.add(searchPanel, PAGE_SEARCH);
        mainContentCardPanel.add(exportPanel, PAGE_EXPORT);
        mainContentCardPanel.add(settingsPanel, PAGE_SETTINGS);
        mainContentCardPanel.add(aboutPanel, PAGE_ABOUT);

        rightContainer.add(mainContentCardPanel, BorderLayout.CENTER);
        add(rightContainer, BorderLayout.CENTER);

        navigateTo(PAGE_DASHBOARD);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIUtil.COLOR_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(245, getHeight()));

        // Sidebar Branding / Logo Header
        JPanel brandPanel = new JPanel(new BorderLayout(10, 4));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(30, 41, 59)),
                new EmptyBorder(22, 20, 22, 20)
        ));

        JLabel lblLogo = new JLabel("EXPENSE TRACKER");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLogo.setForeground(Color.WHITE);
        brandPanel.add(lblLogo, BorderLayout.NORTH);

        JLabel lblTag = new JLabel("Java Swing & File Storage");
        lblTag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTag.setForeground(UIUtil.COLOR_TEXT_MUTED);
        brandPanel.add(lblTag, BorderLayout.SOUTH);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Sidebar Menu Buttons
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(14, 10, 14, 10));

        addNavButton(menuPanel, "1. Dashboard", "◈", PAGE_DASHBOARD);
        addNavButton(menuPanel, "2. Transactions", "☰", PAGE_TRANSACTIONS);
        addNavButton(menuPanel, "3. Add Transaction", "✚", PAGE_ADD_TRANSACTION);
        addNavButton(menuPanel, "4. Categories", "❖", PAGE_CATEGORIES);
        addNavButton(menuPanel, "5. Budget & Alerts", "⏱", PAGE_BUDGET);
        addNavButton(menuPanel, "6. Reports & Analytics", "☵", PAGE_REPORTS);
        addNavButton(menuPanel, "7. Search & Filters", "🔍", PAGE_SEARCH);
        addNavButton(menuPanel, "8. Export Data", "⬇", PAGE_EXPORT);
        addNavButton(menuPanel, "9. Settings", "⚙", PAGE_SETTINGS);
        addNavButton(menuPanel, "10. About Project", "ⓘ", PAGE_ABOUT);

        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.setBorder(null);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.getVerticalScrollBar().setUnitIncrement(14);
        sidebar.add(menuScroll, BorderLayout.CENTER);

        // Sidebar Footer
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 2));
        footer.setOpaque(false);
        footer.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(30, 41, 59)),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JLabel lblVer = new JLabel("Version 1.0 — University Edition");
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVer.setForeground(UIUtil.COLOR_TEXT_MUTED);
        footer.add(lblVer);

        JLabel lblStatus = new JLabel("● Storage: CSV Connected");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStatus.setForeground(new Color(34, 197, 94));
        footer.add(lblStatus);

        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavButton(JPanel parent, String title, String icon, String pageKey) {
        JButton btn = new JButton(icon + "  " + title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(203, 213, 225)); // Slate 300
        btn.setBackground(UIUtil.COLOR_SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(225, 40));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!pageKey.equals(activePageKey)) {
                    btn.setBackground(UIUtil.COLOR_SIDEBAR_HOVER);
                    btn.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!pageKey.equals(activePageKey)) {
                    btn.setBackground(UIUtil.COLOR_SIDEBAR_BG);
                    btn.setForeground(new Color(203, 213, 225));
                }
            }
        });

        btn.addActionListener(e -> navigateTo(pageKey));

        sidebarButtons.put(pageKey, btn);
        parent.add(btn);
        parent.add(Box.createVerticalStrut(4));
    }

    private JPanel createHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIUtil.COLOR_BORDER),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Left: Breadcrumb / Active Section Name
        lblHeaderSection = new JLabel("Dashboard Overview");
        lblHeaderSection.setFont(UIUtil.FONT_HEADER);
        lblHeaderSection.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        bar.add(lblHeaderSection, BorderLayout.WEST);

        // Right: Currency Badge + Live Clock
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        lblDemoBadge = new JLabel("DEMO DATA MODE");
        lblDemoBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblDemoBadge.setForeground(Color.WHITE);
        lblDemoBadge.setBackground(new Color(217, 119, 6)); // Amber
        lblDemoBadge.setOpaque(true);
        lblDemoBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
        lblDemoBadge.setVisible(controller.hasSampleData());
        right.add(lblDemoBadge);

        lblCurrencyBadge = new JLabel("Currency: " + CurrencyUtil.getActiveSymbol() + " (" + controller.getSettings().getCurrencyCode() + ")");
        lblCurrencyBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCurrencyBadge.setForeground(UIUtil.COLOR_PRIMARY);
        lblCurrencyBadge.setBackground(new Color(238, 242, 255));
        lblCurrencyBadge.setOpaque(true);
        lblCurrencyBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        right.add(lblCurrencyBadge);

        lblCurrentClock = new JLabel();
        lblCurrentClock.setFont(UIUtil.FONT_REGULAR);
        lblCurrentClock.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        right.add(lblCurrentClock);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    public void navigateTo(String pageKey) {
        this.activePageKey = pageKey;
        cardLayout.show(mainContentCardPanel, pageKey);

        // Update button active highlighting
        for (Map.Entry<String, JButton> entry : sidebarButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(pageKey)) {
                btn.setBackground(UIUtil.COLOR_SIDEBAR_ACTIVE);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                btn.setBackground(UIUtil.COLOR_SIDEBAR_BG);
                btn.setForeground(new Color(203, 213, 225));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }

        // Update Header Title
        String pageTitle = switch (pageKey) {
            case PAGE_DASHBOARD -> "Dashboard Overview";
            case PAGE_TRANSACTIONS -> "Transaction Ledger";
            case PAGE_ADD_TRANSACTION -> "New Transaction";
            case PAGE_CATEGORIES -> "Category Configuration";
            case PAGE_BUDGET -> "Budget Tracking & Threshold Alerts";
            case PAGE_REPORTS -> "Financial Reports & Visual Analytics";
            case PAGE_SEARCH -> "Advanced Search & Filters";
            case PAGE_EXPORT -> "Data Export Center (CSV)";
            case PAGE_SETTINGS -> "System Settings & Backups";
            case PAGE_ABOUT -> "Academic Documentation & Project Info";
            default -> "Personal Expense Tracker";
        };
        lblHeaderSection.setText(pageTitle);

        refreshActivePanel();
    }

    private void handleNavigationCommand(String command) {
        if ("ADD_TRANSACTION_INCOME".equals(command)) {
            navigateTo(PAGE_ADD_TRANSACTION);
            addTransactionPanel.setPrefilledType(TransactionType.INCOME);
        } else if ("ADD_TRANSACTION_EXPENSE".equals(command)) {
            navigateTo(PAGE_ADD_TRANSACTION);
            addTransactionPanel.setPrefilledType(TransactionType.EXPENSE);
        } else if ("ADD_TRANSACTION".equals(command)) {
            navigateTo(PAGE_ADD_TRANSACTION);
        } else if ("TRANSACTIONS".equals(command)) {
            navigateTo(PAGE_TRANSACTIONS);
        } else if ("BUDGET".equals(command)) {
            navigateTo(PAGE_BUDGET);
        } else if ("REPORTS".equals(command)) {
            navigateTo(PAGE_REPORTS);
        }
    }

    public void checkFirstLaunch() {
        if (!controller.getSettings().isFirstLaunchPromptShown()) {
            SwingUtilities.invokeLater(this::promptFirstLaunchSampleData);
        }
    }

    private void promptFirstLaunchSampleData() {
        Object[] options = new Object[] { "Load Sample Data (Recommended)", "Start with Blank Ledger" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "<html><body style='width: 420px; font-family: Segoe UI; font-size: 11pt; line-height: 1.4;'>"
                + "<h3 style='color: #2563EB; margin-top: 0;'>Welcome to Personal Expense Tracker!</h3>"
                + "Would you like to populate the application with realistic <b>Sample Demo Data</b> "
                + "(20 transactions, categories & budgets) to explore charts, reports, and threshold alerts?<br><br>"
                + "• All sample records are clearly labeled as <b>[DEMO DATA]</b>.<br>"
                + "• You can remove all sample records with one click at any time.<br>"
                + "• You can also restore sample data anytime in the <b>Settings</b> menu.<br>"
                + "</body></html>",
                "Initial Setup — Load Sample Data?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        AppSettings s = controller.getSettings();
        s.setFirstLaunchPromptShown(true);
        if (choice == 0) { // Load sample data
            try {
                controller.loadSampleData();
                UIUtil.showSuccess(this, "Sample demo data loaded successfully!\nExplore the Dashboard, Ledgers, Budgets, and Charts.");
            } catch (Exception ex) {
                UIUtil.showError(this, "Failed to load sample data: " + ex.getMessage());
            }
        } else {
            try {
                s.setSampleDataActive(false);
                controller.saveSettings(s);
                UIUtil.showSuccess(this, "Starting with a fresh blank ledger.\nClick '+ Add Transaction' to record your first entry!");
            } catch (Exception ignored) {}
        }
    }

    private void refreshActivePanel() {
        lblCurrencyBadge.setText("Currency: " + CurrencyUtil.getActiveSymbol() + " (" + controller.getSettings().getCurrencyCode() + ")");
        if (lblDemoBadge != null) {
            lblDemoBadge.setVisible(controller.hasSampleData());
        }
        switch (activePageKey) {
            case PAGE_DASHBOARD -> dashboardPanel.refreshData();
            case PAGE_TRANSACTIONS -> transactionPanel.refreshData();
            case PAGE_ADD_TRANSACTION -> addTransactionPanel.refreshData();
            case PAGE_CATEGORIES -> categoryPanel.refreshData();
            case PAGE_BUDGET -> budgetPanel.refreshData();
            case PAGE_REPORTS -> reportsPanel.refreshData();
            case PAGE_SEARCH -> searchPanel.refreshData();
            case PAGE_SETTINGS -> settingsPanel.refreshData();
            case PAGE_ABOUT -> aboutPanel.refreshData();
        }
    }

    private void startClockTimer() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy • hh:mm:ss a");
        Timer timer = new Timer(1000, e -> {
            lblCurrentClock.setText(LocalDateTime.now().format(fmt));
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(() -> {
            dashboardPanel.refreshData();
            transactionPanel.refreshData();
            budgetPanel.refreshData();
            categoryPanel.refreshData();
            reportsPanel.refreshData();
            searchPanel.refreshData();
            settingsPanel.refreshData();
            lblCurrencyBadge.setText("Currency: " + CurrencyUtil.getActiveSymbol() + " (" + controller.getSettings().getCurrencyCode() + ")");
            if (lblDemoBadge != null) {
                lblDemoBadge.setVisible(controller.hasSampleData());
            }
        });
    }

    // Expose panels for automated screenshot capture and tests
    public DashboardPanel getDashboardPanel() { return dashboardPanel; }
    public TransactionPanel getTransactionPanel() { return transactionPanel; }
    public AddTransactionPanel getAddTransactionPanel() { return addTransactionPanel; }
    public CategoryPanel getCategoryPanel() { return categoryPanel; }
    public BudgetPanel getBudgetPanel() { return budgetPanel; }
    public ReportsPanel getReportsPanel() { return reportsPanel; }
    public SearchPanel getSearchPanel() { return searchPanel; }
    public ExportPanel getExportPanel() { return exportPanel; }
    public SettingsPanel getSettingsPanel() { return settingsPanel; }
    public AboutPanel getAboutPanel() { return aboutPanel; }
}
