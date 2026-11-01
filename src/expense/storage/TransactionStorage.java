package expense.storage;

import expense.exception.StorageException;
import expense.model.Transaction;
import expense.util.CsvUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles CSV persistence for Transactions.
 * Demonstrates:
 * - File IO with BufferedReader/BufferedWriter (Unit 3)
 * - Try-with-resources statement (Unit 3)
 * - Robust error handling and exception chaining (Unit 3)
 */
public class TransactionStorage implements DataStorage<Transaction> {

    private static final String CSV_HEADER = "id,type,amount,category,description,date,createdAt,updatedAt";
    private final File file;

    public TransactionStorage(File file) {
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
                throw new StorageException("Failed to initialize transactions file: " + file.getAbsolutePath(), e);
            }
        }
    }

    @Override
    public List<Transaction> loadAll() throws StorageException {
        List<Transaction> list = new ArrayList<>();
        if (!file.exists() || file.length() == 0) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            List<String[]> records = CsvUtil.parseCsv(reader);
            if (records.isEmpty()) {
                return list;
            }

            // Skip header record at index 0
            for (int i = 1; i < records.size(); i++) {
                String[] fields = records.get(i);
                if (fields.length == 0 || (fields.length == 1 && fields[0].isBlank())) {
                    continue;
                }
                try {
                    Transaction txn = Transaction.fromCsvRecord(fields);
                    list.add(txn);
                } catch (Exception ex) {
                    System.err.println("Warning: Skipping corrupted transaction record at row " + (i + 1) + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new StorageException("Error reading transactions from " + file.getAbsolutePath(), e);
        }
        return list;
    }

    @Override
    public void saveAll(List<Transaction> entities) throws StorageException {
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
                for (Transaction t : entities) {
                    String line = CsvUtil.toCsvLine(t.toCsvRecord());
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to write transactions to temp file: " + tempFile.getAbsolutePath(), e);
        }

        // Atomic replace
        try {
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Fallback for filesystems that do not support atomic move
            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new StorageException("Failed to replace transactions file: " + file.getAbsolutePath(), ex);
            }
        }
    }

    @Override
    public void append(Transaction entity) throws StorageException {
        initializeIfMissing();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            String line = CsvUtil.toCsvLine(entity.toCsvRecord());
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new StorageException("Failed to append transaction to " + file.getAbsolutePath(), e);
        }
    }
}
