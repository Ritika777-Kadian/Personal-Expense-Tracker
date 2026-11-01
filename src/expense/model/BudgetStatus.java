package expense.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculated budget status combining configured limit with actual spending.
 */
public class BudgetStatus {

    private final String category;
    private final BigDecimal limit;
    private final BigDecimal spent;
    private final BigDecimal remaining;
    private final double utilizationPercentage;
    private final BudgetAlertLevel alertLevel;

    public BudgetStatus(String category, BigDecimal limit, BigDecimal spent) {
        this.category = category;
        this.limit = limit != null ? limit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.spent = spent != null ? spent.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal diff = this.limit.subtract(this.spent);
        this.remaining = diff.setScale(2, RoundingMode.HALF_UP);

        if (this.limit.compareTo(BigDecimal.ZERO) > 0) {
            double ratio = this.spent.doubleValue() / this.limit.doubleValue();
            this.utilizationPercentage = Math.round(ratio * 10000.0) / 100.0; // 2 decimal places
        } else {
            this.utilizationPercentage = 0.0;
        }

        if (this.utilizationPercentage > 100.0) {
            this.alertLevel = BudgetAlertLevel.EXCEEDED;
        } else if (this.utilizationPercentage >= 80.0) {
            this.alertLevel = BudgetAlertLevel.WARNING_80;
        } else {
            this.alertLevel = BudgetAlertLevel.NORMAL;
        }
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public double getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public BudgetAlertLevel getAlertLevel() {
        return alertLevel;
    }

    public boolean isExceeded() {
        return alertLevel == BudgetAlertLevel.EXCEEDED;
    }

    public boolean isWarning() {
        return alertLevel == BudgetAlertLevel.WARNING_80;
    }

    @Override
    public String toString() {
        return String.format("%s: Spent %.2f of %.2f (%.1f%%) - %s",
                category, spent, limit, utilizationPercentage, alertLevel.getTitle());
    }
}
