package expense.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mathematical and financial calculation utilities.
 * Demonstrates:
 * - Meaningful recursion for future savings projections (Unit 2)
 * - Bitwise operators for filter mask flags (Unit 1)
 * - Safe BigDecimal arithmetic with rounding modes (Unit 1 & 2)
 */
public final class MathUtil {

    // Bitwise flags for filtering criteria (Unit 1: Bitwise operators)
    public static final int FILTER_TYPE     = 1 << 0; // 0001
    public static final int FILTER_CATEGORY = 1 << 1; // 0010
    public static final int FILTER_DATE     = 1 << 2; // 0100
    public static final int FILTER_AMOUNT   = 1 << 3; // 1000

    private MathUtil() {}

    /**
     * Checks if a specific bitwise filter flag is set in the active filter mask.
     */
    public static boolean hasFilterFlag(int mask, int flag) {
        return (mask & flag) != 0;
    }

    /**
     * Calculates percentage with proper rounding.
     */
    public static double calculatePercentage(BigDecimal part, BigDecimal total) {
        if (part == null || total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return part.divide(total, 4, RoundingMode.HALF_UP)
                   .multiply(BigDecimal.valueOf(100))
                   .setScale(2, RoundingMode.HALF_UP)
                   .doubleValue();
    }

    /**
     * Recursive projection of compound savings balance over time.
     * Demonstrates recursion with a base case and recursive step (Unit 2).
     *
     * @param balance Current balance at this month
     * @param monthlySavings Additional amount saved each month
     * @param annualInterestRate Annual interest rate as percentage (e.g., 5.0 for 5%)
     * @param monthsLeft Number of months remaining in projection
     * @return Projected balance after remaining months
     */
    public static BigDecimal projectSavingsRecursive(BigDecimal balance, BigDecimal monthlySavings, 
                                                      double annualInterestRate, int monthsLeft) {
        // Base case: no more months left to project
        if (monthsLeft <= 0) {
            return balance.setScale(2, RoundingMode.HALF_UP);
        }

        // Monthly interest factor = (annualRate / 12) / 100
        BigDecimal monthlyRate = BigDecimal.valueOf(annualInterestRate)
                .divide(BigDecimal.valueOf(1200), 6, RoundingMode.HALF_UP);

        // Interest earned this month on current balance
        BigDecimal interestEarned = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);

        // New balance = current + interest + new monthly savings
        BigDecimal nextBalance = balance.add(interestEarned).add(monthlySavings);

        // Recursive call for the next month
        return projectSavingsRecursive(nextBalance, monthlySavings, annualInterestRate, monthsLeft - 1);
    }
}
