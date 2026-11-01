package expense.service;

import expense.exception.StorageException;
import expense.model.BudgetStatus;
import expense.model.FinancialSummary;
import expense.model.Transaction;
import expense.util.CsvUtil;
import expense.util.CurrencyUtil;
import expense.util.DateUtil;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Service to export transaction histories and financial reports to standard CSV files.
 */
public class ExportService {

    public ExportService() {}

    /**
     * Exports a list of transactions to a CSV file.
     */
    public void exportTransactionsToCsv(List<Transaction> transactions, File destination) throws StorageException {
        if (destination == null) {
            throw new StorageException("Destination file cannot be null");
        }

        File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(destination), StandardCharsets.UTF_8))) {
            // UTF-8 BOM for Microsoft Excel compatibility
            writer.write('\ufeff');

            // Header
            writer.write(CsvUtil.toCsvLine(new String[] {
                    "Transaction ID", "Type", "Amount (" + CurrencyUtil.getActiveSymbol() + ")", 
                    "Category", "Description", "Date"
            }));
            writer.newLine();

            if (transactions != null) {
                for (Transaction t : transactions) {
                    writer.write(CsvUtil.toCsvLine(new String[] {
                            t.getId(),
                            t.getType().getDisplayName(),
                            t.getAmount().toPlainString(),
                            t.getCategory(),
                            t.getDescription(),
                            DateUtil.formatDisplay(t.getDate())
                    }));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to export transactions to " + destination.getAbsolutePath(), e);
        }
    }

    /**
     * Exports a comprehensive monthly report with summary metrics, category breakdown, and budget statuses.
     */
    public void exportMonthlyReportToCsv(int year, int month, FinancialSummary summary, 
                                        List<BudgetStatus> budgetStatuses, 
                                        Map<String, BigDecimal> categoryExpenses, 
                                        File destination) throws StorageException {
        if (destination == null) {
            throw new StorageException("Destination file cannot be null");
        }

        File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(destination), StandardCharsets.UTF_8))) {
            // Excel UTF-8 BOM
            writer.write('\ufeff');

            writer.write("MONTHLY FINANCIAL REPORT - " + DateUtil.formatMonthYear(year, month));
            writer.newLine();
            writer.newLine();

            // Section 1: Summary Metrics
            writer.write("--- FINANCIAL SUMMARY ---");
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Metric", "Value" }));
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Total Income", summary.getTotalIncome().toPlainString() }));
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Total Expenses", summary.getTotalExpense().toPlainString() }));
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Current Balance (Savings)", summary.getNetBalance().toPlainString() }));
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Monthly Budget", summary.getMonthlyBudget().toPlainString() }));
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Remaining Budget", summary.getRemainingBudget().toPlainString() }));
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Savings Percentage", summary.getSavingsPercentage() + "%" }));
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Budget Utilization", summary.getBudgetUtilizationPercentage() + "%" }));
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Highest Spending Category", summary.getHighestSpendingCategory() }));
            writer.newLine();
            writer.newLine();

            // Section 2: Category Breakdown
            writer.write("--- EXPENSE CATEGORY BREAKDOWN ---");
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Category", "Amount", "Share (%)" }));
            writer.newLine();
            for (Map.Entry<String, BigDecimal> entry : categoryExpenses.entrySet()) {
                double pct = summary.getTotalExpense().compareTo(BigDecimal.ZERO) > 0 ?
                        (entry.getValue().doubleValue() / summary.getTotalExpense().doubleValue()) * 100.0 : 0.0;
                writer.write(CsvUtil.toCsvLine(new String[] {
                        entry.getKey(),
                        entry.getValue().toPlainString(),
                        String.format("%.2f%%", pct)
                }));
                writer.newLine();
            }
            writer.newLine();

            // Section 3: Budget Performance
            writer.write("--- BUDGET UTILIZATION STATUS ---");
            writer.newLine();
            writer.write(CsvUtil.toCsvLine(new String[] { "Category", "Budget Limit", "Amount Spent", "Remaining", "Utilization", "Alert Status" }));
            writer.newLine();
            for (BudgetStatus bs : budgetStatuses) {
                writer.write(CsvUtil.toCsvLine(new String[] {
                        bs.getCategory(),
                        bs.getLimit().toPlainString(),
                        bs.getSpent().toPlainString(),
                        bs.getRemaining().toPlainString(),
                        String.format("%.1f%%", bs.getUtilizationPercentage()),
                        bs.getAlertLevel().getTitle()
                }));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new StorageException("Failed to export monthly report to " + destination.getAbsolutePath(), e);
        }
    }
}
