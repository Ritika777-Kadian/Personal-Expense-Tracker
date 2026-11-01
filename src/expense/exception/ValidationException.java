package expense.exception;

/**
 * Thrown when user input fails business rule validation.
 */
public class ValidationException extends ExpenseTrackerException {
    
    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
