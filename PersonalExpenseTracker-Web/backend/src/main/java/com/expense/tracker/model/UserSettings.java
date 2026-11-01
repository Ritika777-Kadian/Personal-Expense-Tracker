package com.expense.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @Column(nullable = false, length = 10)
    private String currencySymbol = "\u20B9";

    @Column(nullable = false, length = 20)
    private String dateFormat = "YYYY-MM-DD";

    @Column(nullable = false, length = 10)
    private String theme = "light";

    @Column(name = "first_launch_shown", nullable = false)
    private boolean firstLaunchPromptShown = false;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public UserSettings() {}

    public UserSettings(User user) {
        this.user = user;
        this.currency = "INR";
        this.currencySymbol = "\u20B9";
        this.dateFormat = "YYYY-MM-DD";
        this.theme = "light";
        this.firstLaunchPromptShown = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) {
        this.currency = currency;
        switch (currency != null ? currency.toUpperCase() : "INR") {
            case "USD" -> this.currencySymbol = "$";
            case "EUR" -> this.currencySymbol = "\u20AC";
            case "GBP" -> this.currencySymbol = "\u00A3";
            case "JPY" -> this.currencySymbol = "\u00A5";
            default -> this.currencySymbol = "\u20B9";
        }
    }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public boolean isFirstLaunchPromptShown() { return firstLaunchPromptShown; }
    public void setFirstLaunchPromptShown(boolean firstLaunchPromptShown) { this.firstLaunchPromptShown = firstLaunchPromptShown; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
