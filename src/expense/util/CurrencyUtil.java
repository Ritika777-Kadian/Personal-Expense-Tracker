package expense.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility for formatting and parsing monetary amounts with high precision using BigDecimal.
 */
public final class CurrencyUtil {

    private static String activeSymbol = "₹";

    private CurrencyUtil() {}

    public static synchronized void setActiveSymbol(String symbol) {
        if (symbol != null && !symbol.isBlank()) {
            activeSymbol = symbol.trim();
        }
    }

    public static synchronized String getActiveSymbol() {
        return activeSymbol;
    }

    public static String format(BigDecimal amount) {
        return format(amount, activeSymbol);
    }

    public static String format(BigDecimal amount, String symbol) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        String formatted = df.format(amount.setScale(2, RoundingMode.HALF_UP));
        return (symbol != null ? symbol + " " : "") + formatted;
    }

    public static BigDecimal parseAmount(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Amount cannot be empty");
        }
        // Remove currency symbols, commas, spaces
        String cleaned = text.replaceAll("[^0-9.-]", "").trim();
        if (cleaned.isEmpty() || cleaned.equals("-") || cleaned.equals(".")) {
            throw new IllegalArgumentException("Invalid monetary amount: " + text);
        }
        return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
    }
}
