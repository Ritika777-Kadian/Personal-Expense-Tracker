package expense;

import expense.controller.ExpenseController;
import expense.model.Budget;
import expense.model.Transaction;
import expense.model.TransactionType;
import expense.view.MainFrame;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Application Entry Point.
 * Initializes services, seeds initial demo transactions if empty,
 * applies desktop rendering hints, and launches the Swing GUI on the Event Dispatch Thread.
 */
public class Main {

    public static void main(String[] args) {
        // Set Swing anti-aliasing hints
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Try setting system look and feel
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // Fallback to default Swing Look & Feel
                }

                // Initialize Controller & Storage
                ExpenseController controller = new ExpenseController();

                // Create and display main application window
                MainFrame frame = new MainFrame(controller);
                frame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Fatal initialization error: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Seeds initial realistic sample transactions and budgets
     * so that the application opens with an immediately engaging, fully populated dashboard.
     */
    public static void seedInitialSampleData(ExpenseController controller) {
        try {
            controller.loadSampleData();
            System.out.println("Initialized Personal Expense Tracker with sample demo data (20 records).");
        } catch (Exception e) {
            System.err.println("Note: Sample data initialization skipped: " + e.getMessage());
        }
    }
}
