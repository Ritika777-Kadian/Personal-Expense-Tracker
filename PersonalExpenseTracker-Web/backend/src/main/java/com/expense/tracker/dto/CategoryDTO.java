package com.expense.tracker.dto;

import com.expense.tracker.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type is required (INCOME or EXPENSE)")
    private TransactionType type;

    private boolean isDefault;
    private String color;
    private String icon;
    private long transactionCount;

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name, TransactionType type, boolean isDefault, String color, String icon, long transactionCount) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isDefault = isDefault;
        this.color = color;
        this.icon = icon;
        this.transactionCount = transactionCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(long transactionCount) { this.transactionCount = transactionCount; }
}
