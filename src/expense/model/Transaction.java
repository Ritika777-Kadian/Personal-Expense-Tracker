package expense.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single financial transaction (Income or Expense).
 * Demonstrates:
 * - Inheritance (extends BaseEntity)
 * - Interfaces (Comparable<Transaction>)
 * - Method overriding (toString, equals, hashCode, compareTo)
 * - BigDecimal precision for monetary calculations
 * - LocalDate for date representation
 */
public class Transaction extends BaseEntity implements Comparable<Transaction> {

    public static final String DEMO_TAG = "[DEMO DATA]";

    private TransactionType type;
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDate date;

    // Default constructor
    public Transaction() {
        super();
        this.type = TransactionType.EXPENSE;
        this.amount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.category = "";
        this.description = "";
        this.date = LocalDate.now();
    }

    // Overloaded convenience constructor (for new transactions where ID is generated)
    public Transaction(TransactionType type, BigDecimal amount, String category, String description, LocalDate date) {
        super();
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.category = category != null ? category.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.date = date != null ? date : LocalDate.now();
    }

    // Overloaded full constructor
    public Transaction(String id, TransactionType type, BigDecimal amount, String category, String description, LocalDate date) {
        super(id);
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.category = category != null ? category.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.date = date != null ? date : LocalDate.now();
    }

    // Full constructor including audit timestamps
    public Transaction(String id, TransactionType type, BigDecimal amount, String category, 
                       String description, LocalDate date, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.category = category != null ? category.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.date = date != null ? date : LocalDate.now();
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
        touch();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        touch();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category != null ? category.trim() : "";
        touch();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
        touch();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date != null ? date : LocalDate.now();
        touch();
    }

    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    public boolean isDemoData() {
        return description != null && description.contains(DEMO_TAG);
    }

    public String getCleanDescription() {
        if (description == null) return "";
        return description.replace(DEMO_TAG, "").trim();
    }

    @Override
    public String[] toCsvRecord() {
        return new String[] {
            id != null ? id : "",
            type != null ? type.name() : TransactionType.EXPENSE.name(),
            amount != null ? amount.toPlainString() : "0.00",
            category != null ? category : "",
            description != null ? description : "",
            date != null ? date.toString() : LocalDate.now().toString(),
            createdAt != null ? createdAt.toString() : LocalDateTime.now().toString(),
            updatedAt != null ? updatedAt.toString() : LocalDateTime.now().toString()
        };
    }

    public static Transaction fromCsvRecord(String[] record) {
        if (record == null || record.length < 6) {
            throw new IllegalArgumentException("Insufficient columns for Transaction record");
        }
        String id = record[0].replace("\uFEFF", "").trim();
        TransactionType type = TransactionType.fromString(record[1]);
        BigDecimal amount = new BigDecimal(record[2].trim()).setScale(2, RoundingMode.HALF_UP);
        String category = record[3].trim();
        String description = record[4] != null ? record[4] : "";
        LocalDate date = LocalDate.parse(record[5].trim());

        LocalDateTime createdAt = (record.length > 6 && !record[6].isBlank()) ? 
                LocalDateTime.parse(record[6].trim()) : LocalDateTime.now();
        LocalDateTime updatedAt = (record.length > 7 && !record[7].isBlank()) ? 
                LocalDateTime.parse(record[7].trim()) : LocalDateTime.now();

        return new Transaction(id, type, amount, category, description, date, createdAt, updatedAt);
    }

    @Override
    public int compareTo(Transaction other) {
        if (other == null) return 1;
        // Sort descending by date (newest first), then by id descending
        int dateCmp = other.date.compareTo(this.date);
        if (dateCmp != 0) return dateCmp;
        if (this.id != null && other.id != null) {
            return other.id.compareTo(this.id);
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Transaction[id=%s, type=%s, amount=%.2f, category='%s', date=%s]", 
                id, type, amount, category, date);
    }
}
