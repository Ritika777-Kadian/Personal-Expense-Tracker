package com.expense.tracker.dto;

public class UserSettingsDTO {

    private String currency;
    private String currencySymbol;
    private String dateFormat;
    private String theme;
    private boolean firstLaunchPromptShown;

    public UserSettingsDTO() {}

    public UserSettingsDTO(String currency, String currencySymbol, String dateFormat, String theme, boolean firstLaunchPromptShown) {
        this.currency = currency;
        this.currencySymbol = currencySymbol;
        this.dateFormat = dateFormat;
        this.theme = theme;
        this.firstLaunchPromptShown = firstLaunchPromptShown;
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public boolean isFirstLaunchPromptShown() { return firstLaunchPromptShown; }
    public void setFirstLaunchPromptShown(boolean firstLaunchPromptShown) { this.firstLaunchPromptShown = firstLaunchPromptShown; }
}
