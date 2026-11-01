package expense.view.chart;

import expense.service.ReportService.MonthlyTrendPoint;
import expense.util.CurrencyUtil;
import expense.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern vector multi-line trend chart showing 12-month Income and Expense trajectory.
 */
public class TrendLineChartPanel extends JPanel {

    private final List<MonthlyTrendPoint> trendPoints = new ArrayList<>();
    private String title = "Annual Spending & Income Trends";

    public TrendLineChartPanel() {
        setBackground(UIUtil.COLOR_CARD_BG);
        setPreferredSize(new Dimension(540, 240));
    }

    public void setTrendData(List<MonthlyTrendPoint> points, String title) {
        this.trendPoints.clear();
        if (points != null) {
            this.trendPoints.addAll(points);
        }
        if (title != null) {
            this.title = title;
        }
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

        // Chart Title
        g2.setFont(UIUtil.FONT_REGULAR_BOLD);
        g2.setColor(UIUtil.COLOR_TEXT_PRIMARY);
        g2.drawString(title, 16, 22);

        // Legend at top right
        int legendX = w - 190;
        g2.setFont(UIUtil.FONT_SMALL);

        g2.setColor(UIUtil.COLOR_INCOME);
        g2.fillOval(legendX, 12, 8, 8);
        g2.drawString("Income", legendX + 12, 20);

        g2.setColor(UIUtil.COLOR_EXPENSE);
        g2.fillOval(legendX + 75, 12, 8, 8);
        g2.drawString("Expense", legendX + 87, 20);

        if (trendPoints.isEmpty()) {
            g2.dispose();
            return;
        }

        int topY = 45;
        int bottomY = h - 35;
        int chartH = bottomY - topY;
        int leftX = 50;
        int rightX = w - 25;
        int chartW = rightX - leftX;

        // Find max value across all months
        double maxVal = 100.0;
        for (MonthlyTrendPoint p : trendPoints) {
            if (p.getIncome().doubleValue() > maxVal) maxVal = p.getIncome().doubleValue();
            if (p.getExpense().doubleValue() > maxVal) maxVal = p.getExpense().doubleValue();
        }

        // Horizontal Gridlines
        g2.setColor(new Color(241, 245, 249));
        for (int i = 0; i <= 4; i++) {
            int y = bottomY - (chartH * i / 4);
            g2.drawLine(leftX, y, rightX, y);

            // Y-axis tick label
            double tickVal = (maxVal * i) / 4.0;
            g2.setColor(UIUtil.COLOR_TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String tickStr = CurrencyUtil.format(BigDecimal.valueOf(tickVal));
            // simplified
            if (tickVal >= 1000) {
                tickStr = String.format("%.0fk", tickVal / 1000.0);
            } else {
                tickStr = String.format("%.0f", tickVal);
            }
            g2.drawString(tickStr, 10, y + 4);
        }

        int count = trendPoints.size();
        int stepX = count > 1 ? chartW / (count - 1) : chartW;

        int[] xCoords = new int[count];
        int[] yIncome = new int[count];
        int[] yExpense = new int[count];

        for (int i = 0; i < count; i++) {
            MonthlyTrendPoint p = trendPoints.get(i);
            xCoords[i] = leftX + (i * stepX);
            yIncome[i] = bottomY - (int) Math.round((p.getIncome().doubleValue() / maxVal) * chartH);
            yExpense[i] = bottomY - (int) Math.round((p.getExpense().doubleValue() / maxVal) * chartH);

            // Month X-axis label
            g2.setColor(UIUtil.COLOR_TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            String name = p.getMonthName();
            g2.drawString(name, xCoords[i] - fm.stringWidth(name) / 2, bottomY + 18);
        }

        // Draw Income Line (Green)
        Path2D incomePath = new Path2D.Double();
        incomePath.moveTo(xCoords[0], yIncome[0]);
        for (int i = 1; i < count; i++) {
            incomePath.lineTo(xCoords[i], yIncome[i]);
        }
        g2.setColor(UIUtil.COLOR_INCOME);
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(incomePath);

        // Draw Expense Line (Red)
        Path2D expensePath = new Path2D.Double();
        expensePath.moveTo(xCoords[0], yExpense[0]);
        for (int i = 1; i < count; i++) {
            expensePath.lineTo(xCoords[i], yExpense[i]);
        }
        g2.setColor(UIUtil.COLOR_EXPENSE);
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(expensePath);

        // Draw Point Dots
        for (int i = 0; i < count; i++) {
            // Income dot
            g2.setColor(Color.WHITE);
            g2.fillOval(xCoords[i] - 4, yIncome[i] - 4, 8, 8);
            g2.setColor(UIUtil.COLOR_INCOME);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawOval(xCoords[i] - 4, yIncome[i] - 4, 8, 8);

            // Expense dot
            g2.setColor(Color.WHITE);
            g2.fillOval(xCoords[i] - 4, yExpense[i] - 4, 8, 8);
            g2.setColor(UIUtil.COLOR_EXPENSE);
            g2.drawOval(xCoords[i] - 4, yExpense[i] - 4, 8, 8);
        }

        g2.dispose();
    }
}
