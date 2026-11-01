package expense.storage;

import expense.exception.StorageException;
import expense.model.AppSettings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Handles text-based persistence for application settings and student project details.
 */
public class SettingsStorage {

    private final File file;

    public SettingsStorage(File file) {
        this.file = file;
    }

    public AppSettings load() throws StorageException {
        AppSettings settings = new AppSettings();
        if (!file.exists()) {
            save(settings);
            return settings;
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            props.load(reader);

            if (props.containsKey("currencySymbol")) {
                settings.setCurrencySymbol(props.getProperty("currencySymbol"));
            }
            if (props.containsKey("currencyCode")) {
                settings.setCurrencyCode(props.getProperty("currencyCode"));
            }
            if (props.containsKey("themeName")) {
                settings.setThemeName(props.getProperty("themeName"));
            }
            if (props.containsKey("studentName")) {
                settings.setStudentName(props.getProperty("studentName"));
            }
            if (props.containsKey("studentRollNo")) {
                settings.setStudentRollNo(props.getProperty("studentRollNo"));
            }
            if (props.containsKey("department")) {
                settings.setDepartment(props.getProperty("department"));
            }
            if (props.containsKey("collegeName")) {
                settings.setCollegeName(props.getProperty("collegeName"));
            }
            if (props.containsKey("lastBackupDate")) {
                settings.setLastBackupDate(props.getProperty("lastBackupDate"));
            }
            if (props.containsKey("firstLaunchPromptShown")) {
                settings.setFirstLaunchPromptShown(Boolean.parseBoolean(props.getProperty("firstLaunchPromptShown")));
            }
            if (props.containsKey("sampleDataActive")) {
                settings.setSampleDataActive(Boolean.parseBoolean(props.getProperty("sampleDataActive")));
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read settings from " + file.getAbsolutePath(), e);
        }
        return settings;
    }

    public void save(AppSettings settings) throws StorageException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Properties props = new Properties();
        props.setProperty("currencySymbol", settings.getCurrencySymbol());
        props.setProperty("currencyCode", settings.getCurrencyCode());
        props.setProperty("themeName", settings.getThemeName());
        props.setProperty("studentName", settings.getStudentName());
        props.setProperty("studentRollNo", settings.getStudentRollNo());
        props.setProperty("department", settings.getDepartment());
        props.setProperty("collegeName", settings.getCollegeName());
        props.setProperty("lastBackupDate", settings.getLastBackupDate());
        props.setProperty("firstLaunchPromptShown", Boolean.toString(settings.isFirstLaunchPromptShown()));
        props.setProperty("sampleDataActive", Boolean.toString(settings.isSampleDataActive()));

        try (OutputStream out = new FileOutputStream(file);
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            props.store(writer, "Personal Expense Tracker Settings");
        } catch (IOException e) {
            throw new StorageException("Failed to save settings to " + file.getAbsolutePath(), e);
        }
    }
}
