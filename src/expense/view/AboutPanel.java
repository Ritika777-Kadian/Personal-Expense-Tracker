package expense.view;

import expense.controller.ExpenseController;
import expense.exception.StorageException;
import expense.model.AppSettings;
import expense.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Screen providing academic project documentation, student details,
 * system specifications, and syllabus alignment.
 */
public class AboutPanel extends JPanel {

    private final ExpenseController controller;

    private JTextField tfStudentName;
    private JTextField tfRollNo;
    private JTextField tfDept;
    private JTextField tfCollege;

    public AboutPanel(ExpenseController controller) {
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

        JLabel lblTitle = new JLabel("About Project & Academic Details");
        lblTitle.setFont(UIUtil.FONT_TITLE);
        lblTitle.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        header.add(lblTitle);

        JLabel lblSub = new JLabel("Individual college practical project built with pure Java Swing and robust File Handling");
        lblSub.setFont(UIUtil.FONT_REGULAR);
        lblSub.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        header.add(lblSub);

        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // 1. Overview Card
        content.add(createOverviewCard());
        content.add(Box.createVerticalStrut(16));

        // 2. Student & Institution Details Card
        content.add(createStudentDetailsCard());
        content.add(Box.createVerticalStrut(16));

        // 3. Technical Specifications Card
        content.add(createSpecsCard());

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        add(sp, BorderLayout.CENTER);
    }

    private JPanel createOverviewCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new BorderLayout(12, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Personal Expense Tracker Desktop Application");
        title.setFont(UIUtil.FONT_SUBTITLE);
        title.setForeground(UIUtil.COLOR_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String overviewText = "<html><body style='width: 650px; font-family: Segoe UI; font-size: 11pt; color: #334155; line-height: 1.5;'>"
                + "<b>Project Objectives:</b>"
                + "<ul>"
                + "<li>Provide a reliable, self-contained desktop system for managing personal incomes and expenditures.</li>"
                + "<li>Implement strict object-oriented programming (OOP) principles, inheritance, abstract base entities, and generic persistence interfaces.</li>"
                + "<li>Demonstrate robust Java File Handling (RFC-4180 CSV serialization, atomic file replacements, and automated ZIP backups) without requiring external SQL databases or internet connectivity.</li>"
                + "<li>Enforce accurate financial calculations using <code>java.math.BigDecimal</code> and modern dates using <code>java.time.LocalDate</code>.</li>"
                + "<li>Deliver an intuitive, modern dashboard with threshold-based budget warnings and vector Swing chart visualizers.</li>"
                + "</ul></body></html>";

        JLabel lblDesc = new JLabel(overviewText);
        card.add(lblDesc, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStudentDetailsCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 10, 6, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("Student & Department Information (Editable)");
        title.setFont(UIUtil.FONT_SUBTITLE);
        title.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        card.add(title, gbc);

        gbc.gridwidth = 1;

        // Student Name
        gbc.gridx = 0; gbc.gridy = 1;
        card.add(new JLabel("Student Name:"), gbc);
        gbc.gridx = 1;
        tfStudentName = UIUtil.createStyledTextField(20);
        card.add(tfStudentName, gbc);

        // Roll No
        gbc.gridx = 0; gbc.gridy = 2;
        card.add(new JLabel("Roll / Registration No:"), gbc);
        gbc.gridx = 1;
        tfRollNo = UIUtil.createStyledTextField(20);
        card.add(tfRollNo, gbc);

        // Department
        gbc.gridx = 0; gbc.gridy = 3;
        card.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        tfDept = UIUtil.createStyledTextField(25);
        card.add(tfDept, gbc);

        // College
        gbc.gridx = 0; gbc.gridy = 4;
        card.add(new JLabel("College / University:"), gbc);
        gbc.gridx = 1;
        tfCollege = UIUtil.createStyledTextField(25);
        card.add(tfCollege, gbc);

        // Save Button
        gbc.gridx = 1; gbc.gridy = 5;
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        btnBar.setOpaque(false);
        JButton btnSave = UIUtil.createPrimaryButton("Save Academic Details");
        btnSave.addActionListener(e -> onSaveStudentDetails());
        btnBar.add(btnSave);
        card.add(btnBar, gbc);

        return card;
    }

    private JPanel createSpecsCard() {
        JPanel card = UIUtil.createCardPanel();
        card.setLayout(new GridLayout(6, 2, 10, 8));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        addSpecRow(card, "Programming Language:", "Java (Java SE 17 / 21 LTS)");
        addSpecRow(card, "GUI Framework:", "Java Swing (Pure native desktop GUI)");
        addSpecRow(card, "Persistence Architecture:", "Java File Handling (transactions.csv, budgets.csv, categories.csv)");
        addSpecRow(card, "Database System:", "None (External database replaced by safe CSV/text storage)");
        addSpecRow(card, "Development IDE:", "IntelliJ IDEA / Eclipse / Command Line");
        addSpecRow(card, "Design Pattern:", "Model-View-Controller (MVC) + Service Layer");

        return card;
    }

    private void addSpecRow(JPanel p, String label, String value) {
        JLabel lblL = new JLabel(label);
        lblL.setFont(UIUtil.FONT_REGULAR_BOLD);
        lblL.setForeground(UIUtil.COLOR_TEXT_PRIMARY);

        JLabel lblV = new JLabel(value);
        lblV.setFont(UIUtil.FONT_REGULAR);
        lblV.setForeground(UIUtil.COLOR_TEXT_SECONDARY);

        p.add(lblL);
        p.add(lblV);
    }

    private void onSaveStudentDetails() {
        AppSettings s = controller.getSettings();
        s.setStudentName(tfStudentName.getText().trim());
        s.setStudentRollNo(tfRollNo.getText().trim());
        s.setDepartment(tfDept.getText().trim());
        s.setCollegeName(tfCollege.getText().trim());

        try {
            controller.saveSettings(s);
            UIUtil.showSuccess(this, "Academic details updated successfully!");
        } catch (StorageException ex) {
            UIUtil.showError(this, "Failed to save details: " + ex.getMessage());
        }
    }

    public void refreshData() {
        AppSettings s = controller.getSettings();
        tfStudentName.setText(s.getStudentName());
        tfRollNo.setText(s.getStudentRollNo());
        tfDept.setText(s.getDepartment());
        tfCollege.setText(s.getCollegeName());
    }
}
