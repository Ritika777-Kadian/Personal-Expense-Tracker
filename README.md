# Personal Expense Tracker Using Java Swing & File Handling
# Live at https://excellent-smart-spend-sync.base44.app
A fully functional, professional-grade desktop **Personal Expense Tracker** application engineered strictly in **Java (Java 17/21 LTS)** using **Java Swing** for its graphical interface and **Java File Handling (RFC-4180 streaming CSV and flat text files)** for local persistence.

Designed as an individual college practical and major project evaluation for university students, strictly adhering to pure Object-Oriented Programming (OOP) paradigms with zero external databases or third-party dependencies.

> [!TIP]
> **🚀 Looking for the Full-Stack Web Edition?**  
> A complete, modern web edition of this platform (**Spring Boot 3 + React 18 + PostgreSQL + Neon + Render + Vercel**) is available in the [`PersonalExpenseTracker-Web/`](PersonalExpenseTracker-Web/README.md) directory, featuring stateless JWT authentication, Recharts analytics, and cloud deployment ready configurations.

---

## Key Highlights

- **Pure Java Implementation:** Built 100% in Java. No external database (MySQL, PostgreSQL, MongoDB, SQLite), no Python/JS/HTML/CSS, and no third-party libraries required.
- **Modern Desktop UI:** Professional dark-slate sidebar navigation (`#0F172A`), top live header with temporal clock, breadcrumbs, demo-mode indicator, and 10 separate feature screens.
- **Financial Dashboard:** Real-time summary cards, custom anti-aliased doughnut chart for expense category distribution, income vs expense comparative bar chart, 12-month trend line graph, and recent transactions ledger.
  - **KPI Clarity:** Clearly distinguishes between **Monthly metrics** (Monthly Income, Monthly Expenses, Monthly Savings, Monthly Budget, Remaining Budget) and **All-Time metrics** (Current Balance / Cash on Hand).
- **Proactive Budget Alerts:** Automated threshold monitoring triggering amber warnings at 80% utilization and red alerts when budgets are exceeded (>100%).
- **Interactive Sample Data Management:**
  - First-launch interactive dialog asking whether to seed sample data or start fresh with a blank ledger.
  - Generates 20 realistic, category-distributed sample transactions and tiered budgets.
  - Every sample transaction is clearly labeled with `[DEMO DATA]` and visually styled in amber.
  - One-click purge on the Dashboard or in Settings safely removes **only** demo records while preserving 100% of personal transactions.
  - Settings allows restoring/loading sample data with duplicate prevention and append/replace safety checks.
- **Robust RFC-4180 Streaming CSV Engine:** Custom character-stream state machine parser supporting:
  - Commas embedded inside transaction descriptions.
  - Escaped double quotes (`""`).
  - Multiline descriptions with embedded newlines (`\n`).
  - Empty fields and trailing separators.
  - Automatic UTF-8 Byte Order Mark (BOM: `\uFEFF`) detection and stripping (Excel-safe).
- **Excel-Compatible CSV Export:** Exports transactions and reports with UTF-8 BOM encoding for direct opening in Microsoft Excel.
- **ZIP Backup & Restore:** Built-in archival and data restoration directly through the UI.
- **Java Syllabus Coverage:** Rigorous implementation of Unit 1 (Fundamentals & Bitwise Flags), Unit 2 (Methods, Varargs, Arrays, Recursion, StringBuilder), and Unit 3 (Inheritance, Abstract Classes, Interfaces, Generics, Custom Exceptions, Collections, File I/O).
- **Automated Verification Suite:** 31 rigorous unit and integration tests executing with 100% pass rate in complete isolation without touching live application data.

---

## Project Structure

```
PersonalExpenseTracker/
├── src/
│   └── expense/
│       ├── Main.java                          # Main entry point & first-launch prompt
│       ├── model/                             # Data entities & models
│       │   ├── Identifiable.java              # Unique ID interface
│       │   ├── CsvSerializable.java           # CSV serialization contract
│       │   ├── BaseEntity.java                # Abstract base entity
│       │   ├── Transaction.java               # Transaction model with demo data support
│       │   ├── TransactionType.java           # INCOME / EXPENSE enum
│       │   ├── Category.java                  # Category entity
│       │   ├── Budget.java                    # Monthly / Category budget
│       │   ├── BudgetStatus.java              # Computed budget metrics
│       │   ├── BudgetAlertLevel.java          # NORMAL, WARNING_80, EXCEEDED
│       │   ├── FinancialSummary.java          # Executive financial KPI metrics
│       │   └── AppSettings.java               # Application & Student preferences
│       ├── exception/                         # Custom Exception hierarchy
│       │   ├── ExpenseTrackerException.java   # Base checked exception
│       │   ├── ValidationException.java       # User input error
│       │   ├── StorageException.java          # File I/O failure
│       │   ├── EntityNotFoundException.java   # Entity missing
│       │   └── DuplicateEntityException.java  # Entity collision
│       ├── storage/                           # File handling & persistence
│       │   ├── DataStorage.java               # Generic storage interface <T>
│       │   ├── TransactionStorage.java        # Atomic RFC-4180 CSV writer for transactions
│       │   ├── CategoryStorage.java           # CSV writer for categories
│       │   ├── BudgetStorage.java             # CSV writer for budgets
│       │   ├── SettingsStorage.java           # Key-value property persistence
│       │   └── FileManager.java               # Central directory, backup & restore
│       ├── service/                           # Business logic layer
│       │   ├── TransactionService.java        # Transaction CRUD & filters
│       │   ├── CategoryService.java           # Category management & cascading renames
│       │   ├── BudgetService.java             # Spending comparisons & alerts
│       │   ├── ReportService.java             # Analytics, summaries & trends
│       │   ├── ExportService.java             # Excel-compatible CSV exports
│       │   └── SampleDataService.java         # Realistic demo seeder & safe purger
│       ├── controller/                        # Controller & Observer pattern
│       │   └── ExpenseController.java         # Central coordinator & UI event bus
│       ├── util/                              # Reusable utilities
│       │   ├── CurrencyUtil.java              # BigDecimal formatting & symbols
│       │   ├── DateUtil.java                  # LocalDate formatting & date ranges
│       │   ├── ValidationUtil.java            # Varargs defensive validation
│       │   ├── CsvUtil.java                   # RFC-4180 streaming character parser
│       │   ├── MathUtil.java                  # Bitwise flags & recursive projection
│       │   ├── UIUtil.java                    # Colors, fonts, and button decorators
│       │   └── ScreenshotGenerator.java       # Automated Swing UI screenshot tool
│       ├── view/                              # Java Swing GUI Panels
│       │   ├── MainFrame.java                 # Main window & dark sidebar navigation
│       │   ├── DashboardPanel.java            # Executive financial dashboard & demo banner
│       │   ├── TransactionPanel.java          # Ledger table, row double-click & modal edit
│       │   ├── AddTransactionPanel.java       # Validated transaction creation form
│       │   ├── CategoryPanel.java             # Category manager with relational safety
│       │   ├── BudgetPanel.java               # Budget tracker & progress bars
│       │   ├── ReportsPanel.java              # Analytics & date-range summaries
│       │   ├── SearchPanel.java               # Multi-criteria search engine
│       │   ├── ExportPanel.java               # Centralized data export hub
│       │   ├── SettingsPanel.java             # Demo data manager, backup & restore
│       │   ├── AboutPanel.java                # College project info & student details
│       │   ├── chart/                         # Custom Java 2D vector charts
│       │   │   ├── PieChartPanel.java         # Anti-aliased doughnut chart
│       │   │   ├── BarChartPanel.java         # Income vs Expense bar comparison
│       │   │   └── TrendLineChartPanel.java   # 12-Month trend line chart
│       │   └── component/                     # Custom UI widgets
│       │       ├── MetricCard.java            # Colored KPI stat card
│       │       ├── ModernProgressBar.java     # Adaptive-color progress bar
│       │       └── TransactionTableModel.java # Custom table model & demo pill renderer
│       └── test/                              # Automated test suite
│           └── ExpenseTrackerTestSuite.java   # 31 comprehensive test cases (isolated)
├── data/                                      # Local persistence files
│   ├── transactions.csv                       # Transaction ledger CSV
│   ├── categories.csv                         # Configured categories CSV
│   ├── budgets.csv                            # Budget targets CSV
│   └── settings.txt                           # App configuration & student details
├── screenshots/                               # Authentic captured UI screenshots
│   ├── 01_dashboard.png
│   ├── 02_transactions.png
│   ├── 03_add_transaction.png
│   ├── 04_categories.png
│   ├── 05_budget_management.png
│   ├── 06_reports_analytics.png
│   ├── 07_search_filters.png
│   ├── 08_export_data.png
│   ├── 09_settings.png
│   └── 10_about_project.png
├── docs/
│   └── PROJECT_DOCUMENTATION.md               # 24-section comprehensive college report
├── build.bat                                  # Windows compilation batch script
├── run.bat                                    # Windows launch batch script
├── test.bat                                   # Windows automated test runner
└── PersonalExpenseTracker.jar                 # Standalone executable JAR file
```

---

## How to Run in IntelliJ IDEA

1. **Open IntelliJ IDEA:**
   - Click `File` -> `Open...`
   - Select the `PersonalExpenseTracker` directory and click `OK`.
2. **Configure Project SDK:**
   - Go to `File` -> `Project Structure` -> `Project` (or press `Ctrl + Alt + Shift + S`).
   - Set **SDK** to **Java 17** or **Java 21**.
   - Ensure the project language level is set to `17` or `21`.
3. **Verify Source Directory:**
   - In `Project Structure` -> `Modules`, ensure the `src` folder is marked as **Sources** (blue folder icon).
4. **Run the Application:**
   - In the Project view, navigate to `src/expense/Main.java`.
   - Right-click `Main.java` and select `Run 'Main.main()'`.
   - The desktop application will launch with a first-launch prompt asking whether you would like to explore with sample demo records or start with a clean slate!

---

## How to Run from Command Line

### Option A: Using the Executable JAR
```cmd
java -jar PersonalExpenseTracker.jar
```
Or double-click `run.bat` on Windows.

### Option B: Compiling and Running from Source
```cmd
# Rebuild from source
build.bat

# Launch application
run.bat

# Run the 31-case automated test suite
test.bat
```

---

## Automated Test Suite (31 Tests — 100% Pass Rate)

To verify all functional requirements, edge cases, and storage isolation:
```cmd
java -cp bin expense.test.ExpenseTrackerTestSuite
```

### Complete Test Verification Matrix:
| Test ID | Description | Result |
| :---: | :--- | :---: |
| **Test 1** | Adding Income Transaction | **PASS** |
| **Test 2** | Adding Expense Transaction | **PASS** |
| **Test 3** | Adding Empty / Null Transaction Rejected | **PASS** |
| **Test 4** | Entering Negative or Zero Amount Rejected | **PASS** |
| **Test 5** | Editing an Existing Transaction | **PASS** |
| **Test 6** | Deleting a Transaction | **PASS** |
| **Test 7** | Searching Transactions by Keyword | **PASS** |
| **Test 8** | Filtering by Category and Date Range | **PASS** |
| **Test 9** | Setting a Monthly Budget Limit | **PASS** |
| **Test 10** | Triggering 80% Budget Warning Threshold | **PASS** |
| **Test 11** | Exceeding Budget Threshold (>100% Critical Alert) | **PASS** |
| **Test 12** | Generating Reports and Financial Summaries | **PASS** |
| **Test 13** | Updating Dashboard Charts Data | **PASS** |
| **Test 14** | Exporting CSV Files (RFC-4180 Escaped & Excel Compatible) | **PASS** |
| **Test 15** | Data Persistence After Reopen | **PASS** |
| **Test 16** | Loading Data from Existing Files | **PASS** |
| **Test 17** | Handling Missing or Malformed Files Gracefully | **PASS** |
| **Test 18** | Resetting Data Safely | **PASS** |
| **Test 19** | Relational Category Deletion Safety Constraint | **PASS** |
| **Test 20** | Syllabus Recursion (Compound Savings Projection) | **PASS** |
| **Test 21** | Syllabus Bitwise Operators (Filter Mask Evaluation) | **PASS** |
| **Test 22** | Load 20 Realistic Sample Transactions & Budgets | **PASS** |
| **Test 23** | All Sample Records Clearly Labeled as [DEMO DATA] | **PASS** |
| **Test 24** | Sample Data Persisted in CSV Files and Computes Metrics | **PASS** |
| **Test 25** | Remove Sample Data Preserves User-Created Transactions | **PASS** |
| **Test 26** | Duplicate Prevention on Repeated Sample Data Loading | **PASS** |
| **Test 27** | CSV Parsing: Commas Inside Descriptions | **PASS** |
| **Test 28** | CSV Parsing: Double Quotation Marks Handling (`""`) | **PASS** |
| **Test 29** | CSV Parsing: Multiline Descriptions Handling (`\n`) | **PASS** |
| **Test 30** | CSV Parsing: UTF-8 Characters and BOM Handling (`\uFEFF`) | **PASS** |
| **Test 31** | Complete Test Isolation: Real Application Data Untouched | **PASS** |

**Summary: 31 PASSED, 0 FAILED (100% Pass Rate).**

---

## Author & Academic Information

- **Student Name:** Ritika Kadian
- **Roll Number:** CS-2024-042
- **Department:** Department of Computer Science & Engineering
- **Institution:** Faculty of Technology & Engineering
- **Course:** Object-Oriented Programming with Java
