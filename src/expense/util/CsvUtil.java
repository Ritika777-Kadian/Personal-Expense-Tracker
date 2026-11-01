package expense.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * RFC-4180 compliant CSV serializer and streaming parser.
 * Correctly handles:
 * - Commas inside quoted descriptions
 * - Double quotation marks ("") inside descriptions
 * - Multiline descriptions containing \n or \r\n
 * - Empty fields (e.g., a,,c)
 * - UTF-8 characters and UTF-8 BOM (\uFEFF)
 * - Safe handling of corrupted/truncated lines
 *
 * Demonstrates:
 * - String handling and StringBuilder (Unit 2)
 * - Loops, state-machine parsing, and conditional logic (Unit 1)
 * - Collections Framework with List and ArrayList (Unit 3)
 */
public final class CsvUtil {

    private static final char BOM = '\uFEFF';

    private CsvUtil() {}

    /**
     * Converts an array of string values into a single RFC-4180 compliant CSV line.
     */
    public static String toCsvLine(String[] fields) {
        if (fields == null || fields.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeField(fields[i]));
        }
        return sb.toString();
    }

    /**
     * Escapes a single field: wraps in quotes if it contains comma, quote, or newline.
     * Doubles any existing quotation marks (" -> "").
     */
    public static String escapeField(String field) {
        if (field == null) {
            return "";
        }
        boolean containsSpecialChar = field.contains(",") || field.contains("\"") || 
                                      field.contains("\n") || field.contains("\r");
        if (!containsSpecialChar) {
            return field;
        }

        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c == '"') {
                sb.append("\"\""); // RFC-4180 escape double quote
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Parses an entire CSV stream into a list of string records.
     * Accurately supports multiline values enclosed in quotes, escaped quotes, commas, and BOM.
     */
    public static List<String[]> parseCsv(Reader reader) throws IOException {
        List<String[]> records = new ArrayList<>();
        if (reader == null) {
            return records;
        }

        List<String> currentRecord = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean insideQuotes = false;
        boolean isFirstChar = true;

        int ch;
        while ((ch = reader.read()) != -1) {
            char c = (char) ch;

            // Strip UTF-8 BOM if present at the very beginning of the stream
            if (isFirstChar) {
                isFirstChar = false;
                if (c == BOM) {
                    continue; // Skip BOM
                }
            }

            if (insideQuotes) {
                if (c == '"') {
                    // Check if it's an escaped quote ("")
                    reader.mark(1);
                    int next = reader.read();
                    if (next == '"') {
                        currentToken.append('"'); // Escaped quote
                    } else {
                        // End of quoted value
                        insideQuotes = false;
                        if (next != -1) {
                            reader.reset();
                        }
                    }
                } else {
                    // All other characters inside quotes (including \n, \r, commas) are preserved
                    currentToken.append(c);
                }
            } else {
                if (c == '"') {
                    insideQuotes = true;
                } else if (c == ',') {
                    currentRecord.add(currentToken.toString());
                    currentToken.setLength(0);
                } else if (c == '\r') {
                    // Handle \r\n or standalone \r
                    reader.mark(1);
                    int next = reader.read();
                    if (next != '\n' && next != -1) {
                        reader.reset();
                    }
                    currentRecord.add(currentToken.toString());
                    currentToken.setLength(0);
                    records.add(currentRecord.toArray(new String[0]));
                    currentRecord = new ArrayList<>();
                } else if (c == '\n') {
                    // Standard newline
                    currentRecord.add(currentToken.toString());
                    currentToken.setLength(0);
                    records.add(currentRecord.toArray(new String[0]));
                    currentRecord = new ArrayList<>();
                } else {
                    currentToken.append(c);
                }
            }
        }

        // Add any remaining token and record after EOF
        if (currentToken.length() > 0 || !currentRecord.isEmpty()) {
            currentRecord.add(currentToken.toString());
            records.add(currentRecord.toArray(new String[0]));
        }

        return records;
    }

    /**
     * Parses a single CSV line into an array of fields.
     */
    public static String[] parseCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }
        try {
            List<String[]> list = parseCsv(new StringReader(line));
            if (!list.isEmpty()) {
                return list.get(0);
            }
        } catch (IOException ignored) {}
        return new String[0];
    }
}
