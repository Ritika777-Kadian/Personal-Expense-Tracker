package expense.storage;

import expense.exception.StorageException;
import expense.model.Budget;
import expense.util.CsvUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles CSV persistence for Budgets.
 */
public class BudgetStorage implements DataStorage<Budget> {

    private static final String CSV_HEADER = "id,category,monthlyLimit,year,month,createdAt,updatedAt";
    private final File file;

    public BudgetStorage(File file) {
        this.file = file;
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public void initializeIfMissing() throws StorageException {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(CSV_HEADER);
                writer.newLine();
            } catch (IOException e) {
                throw new StorageException("Failed to initialize budgets file: " + file.getAbsolutePath(), e);
            }
        }
    }

    @Override
    public List<Budget> loadAll() throws StorageException {
        List<Budget> list = new ArrayList<>();
        if (!file.exists() || file.length() == 0) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            List<String[]> records = CsvUtil.parseCsv(reader);
            for (int i = 1; i < records.size(); i++) {
                String[] fields = records.get(i);
                if (fields.length < 5 || (fields.length == 1 && fields[0].isBlank())) continue;
                try {
                    Budget budget = Budget.fromCsvRecord(fields);
                    list.add(budget);
                } catch (Exception ex) {
                    System.err.println("Warning: Skipping corrupted budget at row " + (i + 1) + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new StorageException("Error reading budgets from " + file.getAbsolutePath(), e);
        }
        return list;
    }

    @Override
    public void saveAll(List<Budget> entities) throws StorageException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        File tempFile = new File(file.getAbsolutePath() + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            if (entities != null) {
                for (Budget b : entities) {
                    writer.write(CsvUtil.toCsvLine(b.toCsvRecord()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to write budgets to temp file: " + tempFile.getAbsolutePath(), e);
        }

        try {
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new StorageException("Failed to replace budgets file: " + file.getAbsolutePath(), ex);
            }
        }
    }

    @Override
    public void append(Budget entity) throws StorageException {
        initializeIfMissing();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            writer.write(CsvUtil.toCsvLine(entity.toCsvRecord()));
            writer.newLine();
        } catch (IOException e) {
            throw new StorageException("Failed to append budget to " + file.getAbsolutePath(), e);
        }
    }
}
