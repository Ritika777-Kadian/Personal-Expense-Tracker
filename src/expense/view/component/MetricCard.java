package expense.view.component;

import expense.util.UIUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern dashboard summary metric card with colored accent, primary figure, and subtitle.
 */
public class MetricCard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final JLabel lblIcon;
    private final Color accentColor;

    public MetricCard(String title, String initialValue, String subtitle, String iconSymbol, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(12, 8));
        setBackground(UIUtil.COLOR_CARD_BG);
        setPreferredSize(new Dimension(220, 105));

        // Rounded border with subtle outline
        setBorder(new CompoundBorder(
                new LineBorder(UIUtil.COLOR_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Header panel: Title and Icon
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(UIUtil.COLOR_TEXT_SECONDARY);
        topPanel.add(lblTitle, BorderLayout.WEST);

        lblIcon = new JLabel(iconSymbol);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblIcon.setForeground(accentColor);
        topPanel.add(lblIcon, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Value in center
        lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(UIUtil.COLOR_TEXT_PRIMARY);
        add(lblValue, BorderLayout.CENTER);

        // Subtitle at bottom
        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UIUtil.FONT_SMALL);
        lblSubtitle.setForeground(UIUtil.COLOR_TEXT_MUTED);
        add(lblSubtitle, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    public void setSubtitleColor(Color color) {
        lblSubtitle.setForeground(color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw colored top accent strip
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
        g2.dispose();
    }
}
