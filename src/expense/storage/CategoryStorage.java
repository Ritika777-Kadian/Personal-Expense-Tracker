package expense.storage;

import expense.exception.StorageException;
import expense.model.Category;
import expense.model.TransactionType;
import expense.util.CsvUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles CSV persistence for Categories, including initial default seed data.
 */
public class CategoryStorage implements DataStorage<Category> {

    private static final String CSV_HEADER = "id,name,type,isDefault,createdAt,updatedAt";
    private final File file;

    public CategoryStorage(File file) {
        this.file = file;
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    public static List<Category> getDefaultCategories() {
        List<Category> defaults = new ArrayList<>();
        // Default Income categories
        defaults.add(new Category("Salary", TransactionType.INCOME, true));
        defaults.add(new Category("Business", TransactionType.INCOME, true));
        defaults.add(new Category("Freelance", TransactionType.INCOME, true));
        defaults.add(new Category("Investments", TransactionType.INCOME, true));
        defaults.add(new Category("Other Income", TransactionType.INCOME, true));

        // Default Expense categories
        defaults.add(new Category("Food", TransactionType.EXPENSE, true));
        defaults.add(new Category("Shopping", TransactionType.EXPENSE, true));
        defaults.add(new Category("Transportation", TransactionType.EXPENSE, true));
        defaults.add(new Category("Education", TransactionType.EXPENSE, true));
        defaults.add(new Category("Healthcare", TransactionType.EXPENSE, true));
        defaults.add(new Category("Entertainment", TransactionType.EXPENSE, true));
        defaults.add(new Category("Rent", TransactionType.EXPENSE, true));
        defaults.add(new Category("Bills", TransactionType.EXPENSE, true));
        defaults.add(new Category("Travel", TransactionType.EXPENSE, true));
        defaults.add(new Category("Other Expenses", TransactionType.EXPENSE, true));

        return defaults;
    }

    @Override
    public void initializeIfMissing() throws StorageException {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            saveAll(getDefaultCategories());
        }
    }

    @Override
    public List<Category> loadAll() throws StorageException {
        List<Category> list = new ArrayList<>();
        if (!file.exists() || file.length() == 0) {
            initializeIfMissing();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            List<String[]> records = CsvUtil.parseCsv(reader);
            for (int i = 1; i < records.size(); i++) {
                String[] fields = records.get(i);
                if (fields.length < 3 || (fields.length == 1 && fields[0].isBlank())) continue;
                try {
                    Category cat = Category.fromCsvRecord(fields);
                    list.add(cat);
                } catch (Exception ex) {
                    System.err.println("Warning: Skipping corrupted category at row " + (i + 1) + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new StorageException("Error reading categories from " + file.getAbsolutePath(), e);
        }

        if (list.isEmpty()) {
            list.addAll(getDefaultCategories());
            saveAll(list);
        }
        return list;
    }

    @Override
    public void saveAll(List<Category> entities) throws StorageException {
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
                for (Category c : entities) {
                    writer.write(CsvUtil.toCsvLine(c.toCsvRecord()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed writing categories to temp file: " + tempFile.getAbsolutePath(), e);
        }

        try {
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new StorageException("Failed to update categories file: " + file.getAbsolutePath(), ex);
            }
        }
    }

    @Override
    public void append(Category entity) throws StorageException {
        initializeIfMissing();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            writer.write(CsvUtil.toCsvLine(entity.toCsvRecord()));
            writer.newLine();
        } catch (IOException e) {
            throw new StorageException("Failed to append category to " + file.getAbsolutePath(), e);
        }
    }
}
