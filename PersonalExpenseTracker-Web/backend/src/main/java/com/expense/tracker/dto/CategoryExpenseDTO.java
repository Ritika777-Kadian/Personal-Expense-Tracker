package com.expense.tracker.dto;

import java.math.BigDecimal;

public class CategoryExpenseDTO {

    private String category;
    private BigDecimal amount;
    private double percentage;
    private String color;
    private int count;

    public CategoryExpenseDTO() {}

    public CategoryExpenseDTO(String category, BigDecimal amount, double percentage, String color, int count) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
        this.color = color;
        this.count = count;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
