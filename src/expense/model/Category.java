package expense.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a spending or earning category.
 * Demonstrates inheritance from BaseEntity and encapsulation.
 */
public class Category extends BaseEntity implements Comparable<Category> {

    private String name;
    private TransactionType type;
    private boolean isDefault;

    public Category() {
        super();
        this.name = "";
        this.type = TransactionType.EXPENSE;
        this.isDefault = false;
    }

    public Category(String name, TransactionType type) {
        this(name, type, false);
    }

    public Category(String name, TransactionType type, boolean isDefault) {
        super(name != null ? name.trim().toLowerCase().replaceAll("\\s+", "_") : null);
        this.name = name != null ? name.trim() : "";
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.isDefault = isDefault;
    }

    public Category(String id, String name, TransactionType type, boolean isDefault) {
        super(id);
        this.name = name != null ? name.trim() : "";
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.isDefault = isDefault;
    }

    public Category(String id, String name, TransactionType type, boolean isDefault,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.name = name != null ? name.trim() : "";
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
        touch();
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
        touch();
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
        touch();
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    @Override
    public String[] toCsvRecord() {
        return new String[] {
            id != null ? id : "",
            name != null ? name : "",
            type != null ? type.name() : TransactionType.EXPENSE.name(),
            Boolean.toString(isDefault),
            createdAt != null ? createdAt.toString() : LocalDateTime.now().toString(),
            updatedAt != null ? updatedAt.toString() : LocalDateTime.now().toString()
        };
    }

    public static Category fromCsvRecord(String[] record) {
        if (record == null || record.length < 3) {
            throw new IllegalArgumentException("Insufficient columns for Category record");
        }
        String id = record[0].replace("\uFEFF", "").trim();
        String name = record[1].trim();
        TransactionType type = TransactionType.fromString(record[2]);
        boolean isDef = record.length > 3 && Boolean.parseBoolean(record[3].trim());
        LocalDateTime createdAt = (record.length > 4 && !record[4].isBlank()) ?
                LocalDateTime.parse(record[4].trim()) : LocalDateTime.now();
        LocalDateTime updatedAt = (record.length > 5 && !record[5].isBlank()) ?
                LocalDateTime.parse(record[5].trim()) : LocalDateTime.now();

        return new Category(id, name, type, isDef, createdAt, updatedAt);
    }

    @Override
    public int compareTo(Category other) {
        if (other == null) return 1;
        // First sort by type (Income then Expense), then alphabetical by name
        int typeCmp = this.type.compareTo(other.type);
        if (typeCmp != 0) return typeCmp;
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(name.toLowerCase(), category.name.toLowerCase()) && type == category.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), type);
    }

    @Override
    public String toString() {
        return name;
    }
}
