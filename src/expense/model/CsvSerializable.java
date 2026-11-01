package expense.model;

/**
 * Interface for entities that can be serialized to and deserialized from CSV fields.
 */
public interface CsvSerializable {
    String[] toCsvRecord();
}
