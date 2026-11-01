package expense.storage;

import expense.exception.StorageException;
import expense.model.Identifiable;

import java.util.List;

/**
 * Generic storage interface for persistent entity operations.
 * Demonstrates:
 * - Generics with type bounds (<T extends Identifiable>) (Unit 3)
 * - Interface definitions (Unit 3)
 * - Exception declarations
 */
public interface DataStorage<T extends Identifiable> {

    List<T> loadAll() throws StorageException;

    void saveAll(List<T> entities) throws StorageException;

    void append(T entity) throws StorageException;

    boolean exists();

    void initializeIfMissing() throws StorageException;
}
