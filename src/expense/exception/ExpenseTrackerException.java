package expense.exception;

/**
 * Base custom checked exception for the Personal Expense Tracker application.
 * Demonstrates inheritance and custom exception hierarchy in Java (Unit 3).
 */
public class ExpenseTrackerException extends Exception {
    
    public ExpenseTrackerException(String message) {
        super(message);
    }
    
    public ExpenseTrackerException(String message, Throwable cause) {
        super(message, cause);
    }
}
