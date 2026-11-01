package expense.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UI Utilities and consistent styling theme for the Expense Tracker.
 * Provides custom colors, fonts, card containers, and button decorators.
 */
public final class UIUtil {

    // Palette Colors
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);        // Royal Blue #2563EB
    public static final Color COLOR_PRIMARY_DARK = new Color(29, 78, 216);   // Darker Blue
    public static final Color COLOR_SIDEBAR_BG = new Color(15, 23, 42);      // Slate 900 #0F172A
    public static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);   // Slate 800
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(37, 99, 235);  // Slate Blue
    public static final Color COLOR_MAIN_BG = new Color(248, 250, 252);      // Light Slate #F8FAFC
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);       // Light Gray #E2E8F0
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);    // Dark text
    public static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);// Subdued text
    public static final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);

    public static final Color COLOR_INCOME = new Color(22, 163, 74);         // Emerald Green #16A34A
    public static final Color COLOR_INCOME_LIGHT = new Color(240, 253, 244);
    public static final Color COLOR_EXPENSE = new Color(220, 38, 38);        // Crimson Red #DC2626
    public static final Color COLOR_EXPENSE_LIGHT = new Color(254, 242, 242);
    public static final Color COLOR_WARNING = new Color(217, 119, 6);        // Amber #D97706
    public static final Color COLOR_WARNING_LIGHT = new Color(254, 243, 199);
    public static final Color COLOR_PURPLE = new Color(124, 58, 237);        // Violet #7C3AED
    public static final Color COLOR_PURPLE_LIGHT = new Color(245, 243, 255);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_REGULAR_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD, 20);

    private UIUtil() {}

    /**
     * Creates a styled card container panel with rounded-like light border and clean padding.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    public static JPanel createCardPanel(Border padding) {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                padding
        ));
        return card;
    }

    /**
     * Styles a modern button with custom background, white text, hover effect, and hand cursor.
     */
    public static JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(FONT_REGULAR_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(8, 16, 8, 16));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                }
            }
        });
        return button;
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, COLOR_PRIMARY, COLOR_PRIMARY_DARK);
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, COLOR_INCOME, new Color(21, 128, 61));
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, COLOR_EXPENSE, new Color(185, 28, 28));
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_REGULAR_BOLD);
        button.setForeground(COLOR_TEXT_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(241, 245, 249));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(Color.WHITE);
                }
            }
        });
        return button;
    }

    /**
     * Applies styling to standard JTable: headers, alternate rows, selection colors, cell heights.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        table.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_REGULAR_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);
    }

    public static JTextField createStyledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(COLOR_TEXT_PRIMARY);
        tf.setBackground(Color.WHITE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    public static <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_REGULAR);
        cb.setBackground(Color.WHITE);
        cb.setForeground(COLOR_TEXT_PRIMARY);
        cb.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));
        return cb;
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int res = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return res == JOptionPane.YES_OPTION;
    }
}
