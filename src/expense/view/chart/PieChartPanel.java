package expense.view.chart;

import expense.util.CurrencyUtil;
import expense.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom-drawn modern vector doughnut / pie chart component for Swing.
 * Renders anti-aliased slices, center cutout, and side legend.
 */
public class PieChartPanel extends JPanel {

    private static final Color[] PALETTE = new Color[] {
            new Color(37, 99, 235),   // Blue
            new Color(16, 185, 129),  // Emerald
            new Color(245, 158, 11),  // Amber
            new Color(239, 68, 68),   // Red
            new Color(139, 92, 246),  // Purple
            new Color(236, 72, 153),  // Pink
            new Color(20, 184, 166),  // Teal
            new Color(249, 115, 22),  // Orange
            new Color(99, 102, 241),  // Indigo
            new Color(107, 114, 128)  // Gray
    };

    private static class Slice {
        String label;
        BigDecimal amount;
        double percentage;
        Color color;

        Slice(String label, BigDecimal amount, double percentage, Color color) {
            this.label = label;
            this.amount = amount;
            this.percentage = percentage;
            this.color = color;
        }
    }

    private final List<Slice> slices = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String centerLabel = "Total Expenses";

    public PieChartPanel() {
        setBackground(UIUtil.COLOR_CARD_BG);
        setPreferredSize(new Dimension(360, 240));
    }

    public void setData(Map<String, BigDecimal> data, String centerLabel) {
        this.slices.clear();
        this.centerLabel = centerLabel != null ? centerLabel : "Total";
        this.totalAmount = BigDecimal.ZERO;

        if (data != null && !data.isEmpty()) {
            for (BigDecimal v : data.values()) {
                this.totalAmount = this.totalAmount.add(v);
            }

            int colorIdx = 0;
            for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
                double pct = totalAmount.compareTo(BigDecimal.ZERO) > 0 ?
                        (entry.getValue().doubleValue() / totalAmount.doubleValue()) * 100.0 : 0.0;
                Color col = PALETTE[colorIdx % PALETTE.length];
                slices.add(new Slice(entry.getKey(), entry.getValue(), pct, col));
                colorIdx++;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (slices.isEmpty() || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            drawEmptyState(g2, width, height);
            g2.dispose();
            return;
        }

        // Layout: Left = Doughnut Chart, Right = Legend
        int legendWidth = Math.min(180, width / 2);
        int chartAreaWidth = width - legendWidth;
        int diameter = Math.min(chartAreaWidth - 30, height - 30);
        diameter = Math.max(diameter, 80);

        int chartX = 15 + (chartAreaWidth - 30 - diameter) / 2;
        int chartY = (height - diameter) / 2;

        // Draw slices
        double currentAngle = 90.0; // Start at 12 o'clock
        for (Slice slice : slices) {
            double extent = (slice.percentage / 100.0) * 360.0;
            g2.setColor(slice.color);
            g2.fill(new Arc2D.Double(chartX, chartY, diameter, diameter, currentAngle, -extent, Arc2D.PIE));
            currentAngle -= extent;
        }

        // Draw Doughnut hole (center cutout)
        int holeDiameter = (int) (diameter * 0.58);
        int holeX = chartX + (diameter - holeDiameter) / 2;
        int holeY = chartY + (diameter - holeDiameter) / 2;

        g2.setColor(getBackground());
        g2.fill(new Ellipse2D.Double(holeX, holeY, holeDiameter, holeDiameter));

        // Center Text
        g2.setColor(UIUtil.COLOR_TEXT_SECONDARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fmLabel = g2.getFontMetrics();
        int labelW = fmLabel.stringWidth(centerLabel);
        g2.drawString(centerLabel, holeX + (holeDiameter - labelW) / 2, holeY + holeDiameter / 2 - 4);

        g2.setColor(UIUtil.COLOR_TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fmVal = g2.getFontMetrics();
        String totalStr = CurrencyUtil.format(totalAmount);
        int valW = fmVal.stringWidth(totalStr);
        g2.drawString(totalStr, holeX + (holeDiameter - valW) / 2, holeY + holeDiameter / 2 + 14);

        // Draw Legend on the right
        int legendX = chartAreaWidth + 10;
        int legendY = 20;
        int maxItems = Math.min(slices.size(), 6);

        g2.setFont(UIUtil.FONT_SMALL);
        FontMetrics fmL = g2.getFontMetrics();

        for (int i = 0; i < maxItems; i++) {
            Slice s = slices.get(i);
            // Color dot
            g2.setColor(s.color);
            g2.fillOval(legendX, legendY + 2, 9, 9);

            // Label
            g2.setColor(UIUtil.COLOR_TEXT_PRIMARY);
            String itemText = s.label.length() > 12 ? s.label.substring(0, 10) + ".." : s.label;
            g2.drawString(itemText, legendX + 16, legendY + 11);

            // Percentage
            g2.setColor(UIUtil.COLOR_TEXT_SECONDARY);
            String pctStr = String.format("%.1f%%", s.percentage);
            g2.drawString(pctStr, legendX + 110, legendY + 11);

            legendY += 24;
        }

        if (slices.size() > 6) {
            g2.setColor(UIUtil.COLOR_TEXT_MUTED);
            g2.drawString("+" + (slices.size() - 6) + " more categories", legendX + 16, legendY + 10);
        }

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2, int width, int height) {
        g2.setColor(UIUtil.COLOR_TEXT_MUTED);
        g2.setFont(UIUtil.FONT_REGULAR);
        String msg = "No expense data for this period";
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(msg);
        g2.drawString(msg, (width - w) / 2, height / 2);
    }
}
