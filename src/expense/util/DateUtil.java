package expense.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Utility methods for date handling and formatting.
 * Demonstrates static utility methods and modern java.time API.
 */
public final class DateUtil {

    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private DateUtil() {
        // Prevent instantiation
    }

    public static String formatDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_FORMATTER);
    }

    public static String formatIso(LocalDate date) {
        if (date == null) return "";
        return date.format(ISO_FORMATTER);
    }

    public static String formatMonthYear(int year, int month) {
        return YearMonth.of(year, month).format(MONTH_YEAR_FORMATTER);
    }

    public static LocalDate parse(String text) {
        if (text == null || text.isBlank()) return null;
        text = text.trim();
        try {
            return LocalDate.parse(text, ISO_FORMATTER);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(text, DISPLAY_FORMATTER);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (DateTimeParseException ignored) {
        }
        throw new IllegalArgumentException("Invalid date format: " + text + ". Expected yyyy-MM-dd or dd-MMM-yyyy");
    }

    public static LocalDate getStartOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    public static LocalDate getEndOfMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return ym.atEndOfMonth();
    }

    public static boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) return false;
        if (start != null && date.isBefore(start)) return false;
        if (end != null && date.isAfter(end)) return false;
        return true;
    }
}
