package expense.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Encapsulates application configuration, theme, currency, and academic project metadata.
 */
public class AppSettings implements Serializable {

    private String currencySymbol;
    private String currencyCode;
    private String themeName;
    private String studentName;
    private String studentRollNo;
    private String department;
    private String collegeName;
    private String lastBackupDate;
    private boolean firstLaunchPromptShown;
    private boolean sampleDataActive;

    public AppSettings() {
        this.currencySymbol = "₹";
        this.currencyCode = "INR";
        this.themeName = "Modern Light";
        this.studentName = "Ritika Kadian";
        this.studentRollNo = "CS-2024-042";
        this.department = "Department of Computer Science & Engineering";
        this.collegeName = "Faculty of Technology & Engineering";
        this.lastBackupDate = "Never";
        this.firstLaunchPromptShown = false;
        this.sampleDataActive = false;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getLastBackupDate() {
        return lastBackupDate;
    }

    public void setLastBackupDate(String lastBackupDate) {
        this.lastBackupDate = lastBackupDate;
    }

    public boolean isFirstLaunchPromptShown() {
        return firstLaunchPromptShown;
    }

    public void setFirstLaunchPromptShown(boolean firstLaunchPromptShown) {
        this.firstLaunchPromptShown = firstLaunchPromptShown;
    }

    public boolean isSampleDataActive() {
        return sampleDataActive;
    }

    public void setSampleDataActive(boolean sampleDataActive) {
        this.sampleDataActive = sampleDataActive;
    }
}
