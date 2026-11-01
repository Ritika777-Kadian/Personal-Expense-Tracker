package expense.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Represents a spending limit for a specific category or overall monthly expenses.
 */
public class Budget extends BaseEntity implements Comparable<Budget> {

    public static final String OVERALL_CATEGORY = "Overall";

    private String category;
    private BigDecimal monthlyLimit;
    private int year;
    private int month; // 1 to 12

    public Budget() {
        super();
        this.category = OVERALL_CATEGORY;
        this.monthlyLimit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        YearMonth now = YearMonth.now();
        this.year = now.getYear();
        this.month = now.getMonthValue();
    }

    public Budget(String category, BigDecimal monthlyLimit, int year, int month) {
        super(buildBudgetId(category, year, month));
        this.category = (category == null || category.isBlank()) ? OVERALL_CATEGORY : category.trim();
        this.monthlyLimit = monthlyLimit != null ? monthlyLimit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.year = year;
        this.month = month;
    }

    public Budget(String id, String category, BigDecimal monthlyLimit, int year, int month,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.category = (category == null || category.isBlank()) ? OVERALL_CATEGORY : category.trim();
        this.monthlyLimit = monthlyLimit != null ? monthlyLimit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.year = year;
        this.month = month;
    }

    public static String buildBudgetId(String category, int year, int month) {
        String catPart = (category == null || category.isBlank()) ? OVERALL_CATEGORY : category.trim().replaceAll("\\s+", "_");
        return String.format("BGT-%d-%02d-%s", year, month, catPart);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = (category == null || category.isBlank()) ? OVERALL_CATEGORY : category.trim();
        touch();
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit != null ? monthlyLimit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        touch();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
        touch();
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
        touch();
    }

    public boolean isOverall() {
        return OVERALL_CATEGORY.equalsIgnoreCase(category);
    }

    public YearMonth getYearMonth() {
        return YearMonth.of(year, month);
    }

    @Override
    public String[] toCsvRecord() {
        return new String[] {
            id != null ? id : buildBudgetId(category, year, month),
            category != null ? category : OVERALL_CATEGORY,
            monthlyLimit != null ? monthlyLimit.toPlainString() : "0.00",
            Integer.toString(year),
            Integer.toString(month),
            createdAt != null ? createdAt.toString() : LocalDateTime.now().toString(),
            updatedAt != null ? updatedAt.toString() : LocalDateTime.now().toString()
        };
    }

    public static Budget fromCsvRecord(String[] record) {
        if (record == null || record.length < 5) {
            throw new IllegalArgumentException("Insufficient columns for Budget record");
        }
        String id = record[0].replace("\uFEFF", "").trim();
        String category = record[1].trim();
        BigDecimal limit = new BigDecimal(record[2].trim()).setScale(2, RoundingMode.HALF_UP);
        int year = Integer.parseInt(record[3].trim());
        int month = Integer.parseInt(record[4].trim());

        LocalDateTime createdAt = (record.length > 5 && !record[5].isBlank()) ?
                LocalDateTime.parse(record[5].trim()) : LocalDateTime.now();
        LocalDateTime updatedAt = (record.length > 6 && !record[6].isBlank()) ?
                LocalDateTime.parse(record[6].trim()) : LocalDateTime.now();

        return new Budget(id, category, limit, year, month, createdAt, updatedAt);
    }

    @Override
    public int compareTo(Budget other) {
        if (other == null) return 1;
        // Compare year desc, month desc, then overall first, then category name
        if (this.year != other.year) return Integer.compare(other.year, this.year);
        if (this.month != other.month) return Integer.compare(other.month, this.month);
        if (this.isOverall() && !other.isOverall()) return -1;
        if (!this.isOverall() && other.isOverall()) return 1;
        return this.category.compareToIgnoreCase(other.category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return year == budget.year && month == budget.month &&
                Objects.equals(category.toLowerCase(), budget.category.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(category.toLowerCase(), year, month);
    }

    @Override
    public String toString() {
        return String.format("Budget[%s: %s (limit=%.2f)]", getYearMonth(), category, monthlyLimit);
    }
}
