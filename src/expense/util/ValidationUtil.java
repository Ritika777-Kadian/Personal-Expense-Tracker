package expense.util;

import expense.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Validation utilities for user input and data model constraints.
 * Demonstrates:
 * - Varargs (Unit 2)
 * - Custom exception throwing (Unit 3)
 * - Defensive programming practices
 */
public final class ValidationUtil {

    private ValidationUtil() {}

    /**
     * Validates that none of the provided string fields are null or blank.
     * Demonstrates Varargs (Variable Arguments) from Unit 2.
     */
    public static void validateNotEmpty(String... fields) throws ValidationException {
        if (fields == null || fields.length == 0) {
            throw new ValidationException("Required fields must not be empty");
        }
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] == null || fields[i].trim().isEmpty()) {
                throw new ValidationException("Field at position " + (i + 1) + " cannot be empty");
            }
        }
    }

    public static void validateFieldNotEmpty(String fieldName, String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " cannot be empty");
        }
    }

    public static void validatePositiveAmount(String fieldName, BigDecimal amount) throws ValidationException {
        if (amount == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be greater than zero. (Entered: " + amount + ")");
        }
    }

    public static void validateDate(String fieldName, LocalDate date) throws ValidationException {
        if (date == null) {
            throw new ValidationException(fieldName, fieldName + " must be a valid date");
        }
        // Reject dates too far in the future (e.g. > 10 years)
        if (date.isAfter(LocalDate.now().plusYears(10))) {
            throw new ValidationException(fieldName, fieldName + " cannot be more than 10 years in the future");
        }
        // Reject dates too far in the past (e.g. before year 2000)
        if (date.isBefore(LocalDate.of(2000, 1, 1))) {
            throw new ValidationException(fieldName, fieldName + " cannot be before year 2000");
        }
    }

    public static void validateDateRange(LocalDate startDate, LocalDate endDate) throws ValidationException {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException("Date Range", "Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }
    }

    public static void validateAmountRange(BigDecimal min, BigDecimal max) throws ValidationException {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new ValidationException("Amount Range", "Minimum amount (" + min + ") cannot be greater than maximum amount (" + max + ")");
        }
    }
}
