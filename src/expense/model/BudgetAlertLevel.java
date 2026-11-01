package expense.model;

import java.awt.Color;

/**
 * Enumeration for budget utilization thresholds and their associated visual alerts.
 */
public enum BudgetAlertLevel {
    NORMAL("Normal", "Spending within safe limits (<80%)", new Color(34, 197, 94)),          // Emerald green
    WARNING_80("Warning", "Budget utilization reached 80% or more!", new Color(245, 158, 11)), // Amber / Orange
    EXCEEDED("Exceeded", "Budget limit exceeded (>100%)!", new Color(239, 68, 68));          // Red

    private final String title;
    private final String description;
    private final Color color;

    BudgetAlertLevel(String title, String description, Color color) {
        this.title = title;
        this.description = description;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }
}
