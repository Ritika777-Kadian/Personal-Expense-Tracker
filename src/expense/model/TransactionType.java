package expense.model;

/**
 * Enumeration representing the type of financial transaction.
 * Demonstrates enum types and switch expressions in Java.
 */
public enum TransactionType {
    INCOME("Income", "+"),
    EXPENSE("Expense", "-");

    private final String displayName;
    private final String sign;

    TransactionType(String displayName, String sign) {
        this.displayName = displayName;
        this.sign = sign;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSign() {
        return sign;
    }

    public static TransactionType fromString(String text) {
        if (text == null) {
            return EXPENSE;
        }
        for (TransactionType type : values()) {
            if (type.name().equalsIgnoreCase(text.trim()) || 
                type.displayName.equalsIgnoreCase(text.trim())) {
                return type;
            }
        }
        return EXPENSE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
