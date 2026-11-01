package expense.exception;

/**
 * Thrown when an expected entity (transaction, category, budget) is not found.
 */
public class EntityNotFoundException extends ExpenseTrackerException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
