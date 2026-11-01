package expense.controller;

import expense.exception.ExpenseTrackerException;
import expense.exception.StorageException;
import expense.model.AppSettings;
import expense.service.*;
import expense.storage.FileManager;
import expense.util.CurrencyUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Controller coordinating data services, user actions, and UI event broadcasts.
 * Implements Observer Pattern for data synchronization across all UI panels.
 */
public class ExpenseController {

    public interface DataChangeListener {
        void onDataChanged();
    }

    private final FileManager fileManager;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final BudgetService budgetService;
    private final ReportService reportService;
    private final ExportService exportService;
    private final SampleDataService sampleDataService;

    private AppSettings settings;
    private final List<DataChangeListener> listeners;

    public ExpenseController() throws StorageException {
        this(new FileManager());
    }

    public ExpenseController(FileManager fileManager) throws StorageException {
        this.fileManager = fileManager != null ? fileManager : new FileManager();
        this.fileManager.initializeAll();

        this.transactionService = new TransactionService(this.fileManager.getTransactionStorage());
        this.categoryService = new CategoryService(this.fileManager.getCategoryStorage());
        this.budgetService = new BudgetService(this.fileManager.getBudgetStorage());
        this.reportService = new ReportService();
        this.exportService = new ExportService();
        this.sampleDataService = new SampleDataService();

        this.listeners = new ArrayList<>();
        loadSettings();
    }

    public void addDataChangeListener(DataChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void notifyDataChanged() {
        for (DataChangeListener listener : listeners) {
            try {
                listener.onDataChanged();
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    private void loadSettings() {
        try {
            this.settings = fileManager.getSettingsStorage().load();
            CurrencyUtil.setActiveSymbol(settings.getCurrencySymbol());
        } catch (StorageException e) {
            this.settings = new AppSettings();
            System.err.println("Could not load settings, using defaults: " + e.getMessage());
        }
    }

    public void saveSettings(AppSettings updatedSettings) throws StorageException {
        this.settings = updatedSettings;
        CurrencyUtil.setActiveSymbol(settings.getCurrencySymbol());
        fileManager.getSettingsStorage().save(settings);
        notifyDataChanged();
    }

    public File backupData(File destinationZip) throws StorageException {
        File zip = fileManager.backupData(destinationZip);
        settings.setLastBackupDate(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
        fileManager.getSettingsStorage().save(settings);
        notifyDataChanged();
        return zip;
    }

    public void restoreData(File backupZip) throws StorageException {
        fileManager.restoreData(backupZip);
        transactionService.reload();
        categoryService.reload();
        budgetService.reload();
        loadSettings();
        notifyDataChanged();
    }

    public void resetAllData() throws StorageException {
        fileManager.resetAllData();
        transactionService.reload();
        categoryService.reload();
        budgetService.reload();
        loadSettings();
        notifyDataChanged();
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public BudgetService getBudgetService() {
        return budgetService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public ExportService getExportService() {
        return exportService;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public AppSettings getSettings() {
        return settings;
    }

    public SampleDataService getSampleDataService() {
        return sampleDataService;
    }

    public void loadSampleData() throws expense.exception.ValidationException, StorageException {
        sampleDataService.loadSampleData(this);
    }

    public int removeSampleData() throws StorageException {
        return sampleDataService.removeSampleData(this, true);
    }

    public boolean hasSampleData() {
        return sampleDataService.hasSampleData(this);
    }

    public int getSampleDataCount() {
        return sampleDataService.getSampleDataCount(this);
    }
}
