package expense.exception;

/**
 * Thrown when attempting to create an entity that already exists.
 */
public class DuplicateEntityException extends ExpenseTrackerException {

    public DuplicateEntityException(String message) {
        super(message);
    }
}
