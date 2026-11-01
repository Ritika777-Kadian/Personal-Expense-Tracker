package expense.model;

import java.time.LocalDateTime;

/**
 * Abstract base class providing common identity and auditing properties.
 * Demonstrates:
 * - Abstract classes and inheritance (Unit 3)
 * - Constructor overloading and the 'this' keyword (Unit 2)
 * - Encapsulation with access modifiers (Unit 3)
 */
public abstract class BaseEntity implements Identifiable, CsvSerializable {

    protected String id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    // Default constructor
    public BaseEntity() {
        this(null);
    }

    // Overloaded constructor with ID
    public BaseEntity(String id) {
        this(id, LocalDateTime.now(), LocalDateTime.now());
    }

    // Overloaded full constructor
    public BaseEntity(String id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
