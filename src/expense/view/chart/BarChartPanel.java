package expense.view.chart;

import expense.util.CurrencyUtil;
import expense.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Modern vector bar chart comparing Income vs Expense.
 */
public class BarChartPanel extends JPanel {

    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expense = BigDecimal.ZERO;
    private String subtitle = "Income vs Expenses";

    public BarChartPanel() {
        setBackground(UIUtil.COLOR_CARD_BG);
        setPreferredSize(new Dimension(360, 240));
    }

    public void setComparisonData(BigDecimal income, BigDecimal expense, String subtitle) {
        this.income = income != null ? income : BigDecimal.ZERO;
        this.expense = expense != null ? expense : BigDecimal.ZERO;
        this.subtitle = subtitle != null ? subtitle : "";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Title
        g2.setFont(UIUtil.FONT_REGULAR_BOLD);
        g2.setColor(UIUtil.COLOR_TEXT_PRIMARY);
        g2.drawString(subtitle, 16, 22);

        // Chart bounds
        int topY = 45;
        int bottomY = h - 45;
        int chartHeight = bottomY - topY;

        double maxVal = Math.max(income.doubleValue(), expense.doubleValue());
        if (maxVal <= 0.0) {
            g2.setColor(UIUtil.COLOR_TEXT_MUTED);
            g2.setFont(UIUtil.FONT_REGULAR);
            String msg = "No income or expense data to display";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        // Draw horizontal grid lines
        g2.setColor(new Color(241, 245, 249));
        g2.drawLine(40, bottomY, w - 20, bottomY);
        g2.drawLine(40, topY + chartHeight / 2, w - 20, topY + chartHeight / 2);
        g2.drawLine(40, topY, w - 20, topY);

        // Calculate bar geometry
        int availableW = w - 80;
        int barW = Math.min(65, availableW / 4);
        int gap = 30;

        int groupX = 40 + (availableW - (barW * 2 + gap)) / 2;

        int incomeBarX = groupX;
        int expenseBarX = groupX + barW + gap;

        int incomeH = (int) Math.round((income.doubleValue() / maxVal) * (chartHeight - 20));
        int expenseH = (int) Math.round((expense.doubleValue() / maxVal) * (chartHeight - 20));

        // Income Bar (Green)
        g2.setColor(UIUtil.COLOR_INCOME);
        g2.fillRoundRect(incomeBarX, bottomY - incomeH, barW, incomeH, 8, 8);

        // Expense Bar (Red)
        g2.setColor(UIUtil.COLOR_EXPENSE);
        g2.fillRoundRect(expenseBarX, bottomY - expenseH, barW, expenseH, 8, 8);

        // Values above bars
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(UIUtil.COLOR_INCOME);
        String incStr = CurrencyUtil.format(income);
        g2.drawString(incStr, incomeBarX + (barW - fm.stringWidth(incStr)) / 2, bottomY - incomeH - 6);

        g2.setColor(UIUtil.COLOR_EXPENSE);
        String expStr = CurrencyUtil.format(expense);
        g2.drawString(expStr, expenseBarX + (barW - fm.stringWidth(expStr)) / 2, bottomY - expenseH - 6);

        // Labels below bars
        g2.setFont(UIUtil.FONT_REGULAR_BOLD);
        g2.setColor(UIUtil.COLOR_TEXT_PRIMARY);

        String lblInc = "Total Income";
        g2.drawString(lblInc, incomeBarX + (barW - fm.stringWidth(lblInc)) / 2, bottomY + 20);

        String lblExp = "Total Expense";
        g2.drawString(lblExp, expenseBarX + (barW - fm.stringWidth(lblExp)) / 2, bottomY + 20);

        g2.dispose();
    }
}
