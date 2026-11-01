package expense.view.component;

import expense.model.Transaction;
import expense.model.TransactionType;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;
import expense.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel and cell renderers for displaying transactions in JTable.
 */
public class TransactionTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
            "ID", "Type", "Amount", "Category", "Description", "Date"
    };

    private final List<Transaction> transactions;

    public TransactionTableModel() {
        this.transactions = new ArrayList<>();
    }

    public TransactionTableModel(List<Transaction> list) {
        this.transactions = new ArrayList<>(list);
    }

    public void setTransactions(List<Transaction> list) {
        this.transactions.clear();
        if (list != null) {
            this.transactions.addAll(list);
        }
        fireTableDataChanged();
    }

    public Transaction getTransactionAt(int row) {
        if (row >= 0 && row < transactions.size()) {
            return transactions.get(row);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return transactions.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= transactions.size()) {
            return null;
        }
        Transaction t = transactions.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> t.getId();
            case 1 -> t.getType();
            case 2 -> t.getAmount();
            case 3 -> t.getCategory();
            case 4 -> t.getDescription();
            case 5 -> t.getDate();
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 3, 4 -> String.class;
            case 1 -> TransactionType.class;
            case 2 -> Object.class;
            case 5 -> Object.class;
            default -> Object.class;
        };
    }

    /**
     * Applies custom renderers to a transaction table.
     */
    public static void applyRenderers(JTable table) {
        // ID Renderer (subtle)
        DefaultTableCellRenderer idRenderer = new DefaultTableCellRenderer();
        idRenderer.setFont(UIUtil.FONT_REGULAR);
        idRenderer.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        idRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        table.getColumnModel().getColumn(0).setCellRenderer(idRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);

        // Type Badge Renderer
        table.getColumnModel().getColumn(1).setCellRenderer(new TypeBadgeRenderer());
        table.getColumnModel().getColumn(1).setPreferredWidth(100);

        // Amount Renderer (Right aligned, colored)
        table.getColumnModel().getColumn(2).setCellRenderer(new AmountRenderer());
        table.getColumnModel().getColumn(2).setPreferredWidth(120);

        // Category Renderer
        DefaultTableCellRenderer catRenderer = new DefaultTableCellRenderer();
        catRenderer.setFont(UIUtil.FONT_REGULAR_BOLD);
        catRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        table.getColumnModel().getColumn(3).setCellRenderer(catRenderer);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);

        // Description Renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DescriptionRenderer());
        table.getColumnModel().getColumn(4).setPreferredWidth(260);

        // Date Renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DateRenderer());
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
    }

    /**
     * Badge renderer for Transaction Type (Pill shape with soft green/red background).
     */
    public static class TypeBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel badge = new JLabel();
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));
            badge.setOpaque(true);

            if (value instanceof TransactionType type) {
                if (type == TransactionType.INCOME) {
                    badge.setText("+ " + type.getDisplayName());
                    badge.setBackground(UIUtil.COLOR_INCOME_LIGHT);
                    badge.setForeground(UIUtil.COLOR_INCOME);
                } else {
                    badge.setText("- " + type.getDisplayName());
                    badge.setBackground(UIUtil.COLOR_EXPENSE_LIGHT);
                    badge.setForeground(UIUtil.COLOR_EXPENSE);
                }
            } else {
                badge.setText(value != null ? value.toString() : "");
            }
            panel.add(badge);
            return panel;
        }
    }

    public static class AmountRenderer extends DefaultTableCellRenderer {
        public AmountRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(new EmptyBorder(0, 10, 0, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(UIUtil.FONT_REGULAR_BOLD);

            if (value instanceof java.math.BigDecimal amt) {
                setText(CurrencyUtil.format(amt));
            }

            // Color based on transaction type if available
            int modelRow = table.convertRowIndexToModel(row);
            if (table.getModel() instanceof TransactionTableModel m) {
                Transaction t = m.getTransactionAt(modelRow);
                if (t != null) {
                    setForeground(isSelected ? table.getSelectionForeground() :
                            (t.isIncome() ? UIUtil.COLOR_INCOME : UIUtil.COLOR_TEXT_PRIMARY));
                }
            }
            return this;
        }
    }

    public static class DescriptionRenderer extends DefaultTableCellRenderer {
        public DescriptionRenderer() {
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int modelRow = table.convertRowIndexToModel(row);
            if (table.getModel() instanceof TransactionTableModel m) {
                Transaction t = m.getTransactionAt(modelRow);
                if (t != null && t.isDemoData()) {
                    if (!isSelected) {
                        setForeground(new Color(180, 83, 9)); // Amber / Orange dark
                    }
                } else {
                    if (!isSelected) {
                        setForeground(UIUtil.COLOR_TEXT_PRIMARY);
                    }
                }
            }
            return this;
        }
    }

    public static class DateRenderer extends DefaultTableCellRenderer {
        public DateRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof java.time.LocalDate date) {
                setText(DateUtil.formatDisplay(date));
            }
            setForeground(isSelected ? table.getSelectionForeground() : UIUtil.COLOR_TEXT_SECONDARY);
            return this;
        }
    }
}
