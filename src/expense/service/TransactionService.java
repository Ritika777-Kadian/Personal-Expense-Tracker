package expense.service;

import expense.exception.EntityNotFoundException;
import expense.exception.StorageException;
import expense.exception.ValidationException;
import expense.model.Transaction;
import expense.model.TransactionType;
import expense.storage.TransactionStorage;
import expense.util.DateUtil;
import expense.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Business service managing transaction CRUD operations, unique ID generation,
 * filtering, searching, and sorting.
 */
public class TransactionService {

    private final TransactionStorage storage;
    private final List<Transaction> transactions;
    private final AtomicLong idSequence;

    public TransactionService(TransactionStorage storage) throws StorageException {
        this.storage = storage;
        this.transactions = new ArrayList<>();
        this.idSequence = new AtomicLong(1000);
        loadTransactions();
    }

    private void loadTransactions() throws StorageException {
        transactions.clear();
        List<Transaction> loaded = storage.loadAll();
        transactions.addAll(loaded);

        long maxSeq = 1000;
        for (Transaction t : transactions) {
            if (t.getId() != null && t.getId().startsWith("TXN-")) {
                try {
                    long seq = Long.parseLong(t.getId().substring(4));
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        idSequence.set(maxSeq);
        Collections.sort(transactions); // Sort newest first
    }

    public synchronized String generateNextId() {
        return "TXN-" + idSequence.incrementAndGet();
    }

    public synchronized List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    public synchronized Transaction getTransactionById(String id) throws EntityNotFoundException {
        if (id == null) throw new EntityNotFoundException("Transaction ID cannot be null");
        return transactions.stream()
                .filter(t -> id.equalsIgnoreCase(t.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + id));
    }

    public synchronized Transaction addTransaction(Transaction txn) throws ValidationException, StorageException {
        validateTransaction(txn);
        if (txn.getId() == null || txn.getId().isBlank()) {
            txn.setId(generateNextId());
        } else {
            // Check for duplicate ID
            boolean exists = transactions.stream().anyMatch(t -> t.getId().equalsIgnoreCase(txn.getId()));
            if (exists) {
                txn.setId(generateNextId());
            }
        }

        transactions.add(0, txn);
        Collections.sort(transactions);
        storage.saveAll(transactions);
        return txn;
    }

    public synchronized Transaction updateTransaction(Transaction updated) throws ValidationException, EntityNotFoundException, StorageException {
        validateTransaction(updated);
        int index = -1;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equalsIgnoreCase(updated.getId())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new EntityNotFoundException("Cannot update; transaction not found: " + updated.getId());
        }

        updated.touch();
        transactions.set(index, updated);
        Collections.sort(transactions);
        storage.saveAll(transactions);
        return updated;
    }

    public synchronized boolean deleteTransaction(String id) throws EntityNotFoundException, StorageException {
        if (id == null) return false;
        Transaction target = getTransactionById(id);
        boolean removed = transactions.remove(target);
        if (removed) {
            storage.saveAll(transactions);
        }
        return removed;
    }

    public void validateTransaction(Transaction txn) throws ValidationException {
        if (txn == null) {
            throw new ValidationException("Transaction", "Transaction object cannot be null");
        }
        ValidationUtil.validateFieldNotEmpty("Category", txn.getCategory());
        ValidationUtil.validatePositiveAmount("Amount", txn.getAmount());
        ValidationUtil.validateDate("Date", txn.getDate());
        if (txn.getType() == null) {
            throw new ValidationException("Type", "Transaction type must be Income or Expense");
        }
    }

    /**
     * Filters transactions using multiple criteria.
     */
    public synchronized List<Transaction> filterTransactions(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String keyword) {

        return transactions.stream().filter(t -> {
            if (type != null && t.getType() != type) {
                return false;
            }
            if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All Categories")) {
                if (!t.getCategory().equalsIgnoreCase(category.trim())) {
                    return false;
                }
            }
            if (startDate != null && t.getDate().isBefore(startDate)) {
                return false;
            }
            if (endDate != null && t.getDate().isAfter(endDate)) {
                return false;
            }
            if (minAmount != null && t.getAmount().compareTo(minAmount) < 0) {
                return false;
            }
            if (maxAmount != null && t.getAmount().compareTo(maxAmount) > 0) {
                return false;
            }
            if (keyword != null && !keyword.isBlank()) {
                String kw = keyword.toLowerCase().trim();
                boolean matchesDesc = t.getDescription() != null && t.getDescription().toLowerCase().contains(kw);
                boolean matchesCat = t.getCategory().toLowerCase().contains(kw);
                boolean matchesId = t.getId().toLowerCase().contains(kw);
                if (!matchesDesc && !matchesCat && !matchesId) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Updates all transactions that have the old category name to the new category name.
     */
    public synchronized int updateCategoryName(String oldName, String newName, TransactionType type) throws StorageException {
        if (oldName == null || newName == null) return 0;
        int count = 0;
        for (Transaction t : transactions) {
            if (t.getType() == type && t.getCategory().equalsIgnoreCase(oldName.trim())) {
                t.setCategory(newName.trim());
                count++;
            }
        }
        if (count > 0) {
            storage.saveAll(transactions);
        }
        return count;
    }

    public synchronized int countTransactionsForCategory(String categoryName, TransactionType type) {
        if (categoryName == null) return 0;
        int count = 0;
        for (Transaction t : transactions) {
            if ((type == null || t.getType() == type) && t.getCategory().equalsIgnoreCase(categoryName.trim())) {
                count++;
            }
        }
        return count;
    }

    public synchronized void reload() throws StorageException {
        loadTransactions();
    }
}
