package expense.exception;

/**
 * Thrown when file IO or data parsing fails during persistence operations.
 */
public class StorageException extends ExpenseTrackerException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
