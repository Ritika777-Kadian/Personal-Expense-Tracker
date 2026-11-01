package expense.view.component;

import expense.util.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Custom painted modern progress bar with automatic threshold color changing.
 */
public class ModernProgressBar extends JComponent {

    private double percentage = 0.0;
    private Color fillColor = UIUtil.COLOR_INCOME;
    private final Color trackColor = new Color(241, 245, 249);

    public ModernProgressBar() {
        setPreferredSize(new Dimension(180, 14));
    }

    public void setPercentage(double pct) {
        this.percentage = Math.max(0.0, pct);
        if (this.percentage > 100.0) {
            fillColor = UIUtil.COLOR_EXPENSE;
        } else if (this.percentage >= 80.0) {
            fillColor = UIUtil.COLOR_WARNING;
        } else {
            fillColor = UIUtil.COLOR_INCOME;
        }
        repaint();
    }

    public double getPercentage() {
        return percentage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = h;

        // Draw track background
        g2.setColor(trackColor);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // Draw fill bar
        double cappedPct = Math.min(percentage, 100.0);
        int fillW = (int) Math.round((w * cappedPct) / 100.0);
        if (fillW > 0) {
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, fillW, h, arc, arc);
        }

        g2.dispose();
    }
}
