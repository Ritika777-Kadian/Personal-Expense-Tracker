package expense.storage;

import expense.exception.StorageException;
import expense.model.AppSettings;
import expense.model.Budget;
import expense.model.Category;
import expense.model.Transaction;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Central coordinator for all file system persistence, data backups, restores, and resets.
 */
public class FileManager {

    public static final String DEFAULT_DATA_DIR = "data";
    private final File dataDir;

    private final TransactionStorage transactionStorage;
    private final CategoryStorage categoryStorage;
    private final BudgetStorage budgetStorage;
    private final SettingsStorage settingsStorage;

    public FileManager() {
        this(new File(DEFAULT_DATA_DIR));
    }

    public FileManager(File dataDir) {
        this.dataDir = dataDir;
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        this.transactionStorage = new TransactionStorage(new File(dataDir, "transactions.csv"));
        this.categoryStorage = new CategoryStorage(new File(dataDir, "categories.csv"));
        this.budgetStorage = new BudgetStorage(new File(dataDir, "budgets.csv"));
        this.settingsStorage = new SettingsStorage(new File(dataDir, "settings.txt"));
    }

    public void initializeAll() throws StorageException {
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        categoryStorage.initializeIfMissing();
        transactionStorage.initializeIfMissing();
        budgetStorage.initializeIfMissing();
    }

    public TransactionStorage getTransactionStorage() {
        return transactionStorage;
    }

    public CategoryStorage getCategoryStorage() {
        return categoryStorage;
    }

    public BudgetStorage getBudgetStorage() {
        return budgetStorage;
    }

    public SettingsStorage getSettingsStorage() {
        return settingsStorage;
    }

    public File getDataDir() {
        return dataDir;
    }

    /**
     * Creates a compressed ZIP backup of the data folder.
     */
    public File backupData(File destinationZip) throws StorageException {
        try {
            if (destinationZip == null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                destinationZip = new File(dataDir, "backup_" + timestamp + ".zip");
            }

            File parent = destinationZip.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            File[] filesToBackup = new File[] {
                    new File(dataDir, "transactions.csv"),
                    new File(dataDir, "categories.csv"),
                    new File(dataDir, "budgets.csv"),
                    new File(dataDir, "settings.txt")
            };

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destinationZip))) {
                byte[] buffer = new byte[4096];
                for (File f : filesToBackup) {
                    if (f.exists() && f.isFile()) {
                        ZipEntry entry = new ZipEntry(f.getName());
                        zos.putNextEntry(entry);
                        try (FileInputStream fis = new FileInputStream(f)) {
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        }
                        zos.closeEntry();
                    }
                }
            }

            return destinationZip;
        } catch (IOException e) {
            throw new StorageException("Failed to create backup ZIP: " + e.getMessage(), e);
        }
    }

    /**
     * Restores data from a backup ZIP file into the data directory.
     */
    public void restoreData(File backupZip) throws StorageException {
        if (backupZip == null || !backupZip.exists()) {
            throw new StorageException("Backup file does not exist: " + (backupZip != null ? backupZip.getAbsolutePath() : "null"));
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupZip))) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = new File(entry.getName()).getName(); // Security against path traversal
                if (fileName.equals("transactions.csv") || fileName.equals("categories.csv") ||
                    fileName.equals("budgets.csv") || fileName.equals("settings.txt")) {
                    File targetFile = new File(dataDir, fileName);
                    try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new StorageException("Failed to restore data from backup: " + e.getMessage(), e);
        }
    }

    /**
     * Resets application data: clears transactions and budgets, reloads default categories.
     */
    public void resetAllData() throws StorageException {
        // Clear transactions
        transactionStorage.saveAll(new java.util.ArrayList<Transaction>());
        // Clear budgets
        budgetStorage.saveAll(new java.util.ArrayList<Budget>());
        // Reset categories to defaults
        categoryStorage.saveAll(CategoryStorage.getDefaultCategories());
        // Reset settings
        settingsStorage.save(new AppSettings());
    }
}
