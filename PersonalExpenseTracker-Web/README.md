# Personal Expense Tracker — Full-Stack Web Application

An enterprise-grade, cloud-deployable full-stack **Personal Expense Tracker** web application engineered with **Spring Boot 3 (Java 21 LTS)**, **React.js 18 (Vite + Tailwind CSS)**, and **PostgreSQL (Neon Cloud / Local)**.

Transformed from an individual Java Swing desktop practical project, this platform brings financial discipline, automated threshold alerts, multi-tenant isolation, and interactive visual analytics to modern web browsers.

---

## 🌟 Key Highlights & Architecture

- **True Full-Stack Decoupled Architecture:** Modern Single Page Application (SPA) communicating with a robust Spring Boot REST API via JSON and Bearer tokens.
- **Enterprise Security & Multi-Tenancy:**
  - Stateless JWT (JSON Web Token) authentication with HMAC-SHA256 signature verification.
  - Password hashing via `BCryptPasswordEncoder` (never stores plain-text passwords).
  - Multi-tenant relational isolation: every entity is bound to the authenticated `User` primary key.
- **High-Precision Financial Math:**
  - Strict utilization of Java `BigDecimal` (`HALF_UP` rounding) for all monetary calculations.
  - `DECIMAL(14,2)` database column precision preventing IEEE-754 floating-point rounding errors.
- **Proactive Budget & Threshold Monitoring:**
  - Overall monthly limits plus category-specific budgets.
  - Automatic **80% Warning** alert and **>100% Critical Exceeded** alert with visual progress bars.
- **Interactive Visual Analytics (Recharts):**
  - Income vs Expenses comparative bar charts.
  - Category spending distribution doughnut chart with percentage tags.
  - 12-month net savings trend progression line chart.
- **Interactive Academic Demo Dataset (Multi-Month):**
  - Seeds **56 realistic transactions across 3 consecutive months** (Salary, Food, Shopping, Travel, Education, Bills, Entertainment, Rent, Healthcare, Transportation).
  - Automatically populates multi-tier monthly budgets.
  - Duplicate prevention safeguards account against re-seeding without clearing.
  - Clearly tagged with `[DEMO DATA]` and styled with amber indicators.
  - One-click safe purge deletes only demo records and preserves 100% of user personal transactions.
- **Excel-Compatible RFC-4180 CSV Export:**
  - Direct browser downloads with UTF-8 Byte Order Mark (`\uFEFF`) and proper quoting for Excel.

---

## 🛠️ Technology Stack

| Layer | Technologies Used | Description |
| :--- | :--- | :--- |
| **Frontend** | React 18, Vite, Tailwind CSS, Recharts, Lucide Icons, React Router v6 | Single-page modern dashboard with navy/teal SaaS design. |
| **Backend** | Java 21 LTS, Spring Boot 3.2, Spring Web, Spring Data JPA, Spring Security | Production-grade RESTful API with validation and JWT filters. |
| **Database** | PostgreSQL 16 (Neon Cloud) / H2 Embedded (Zero-config local fallback) | Relational schema with Foreign Keys and unique constraints. |
| **Deployment** | Vercel (Frontend), Render (Backend), Neon (Database), GitHub (Code) | Cloud-native CI/CD architecture. |

---

## 📁 Project Structure

```
PersonalExpenseTracker-Web/
├── frontend/                               # React 18 + Vite SPA
│   ├── index.html                          # Entry HTML5 shell
│   ├── package.json                        # Frontend dependencies & scripts
│   ├── vite.config.js                      # Vite build & local API proxy
│   ├── tailwind.config.js                  # Modern financial color palette
│   ├── postcss.config.js                   # PostCSS configuration
│   ├── vercel.json                         # Vercel SPA routing configuration
│   ├── .env.example                        # Frontend environment variables template
│   └── src/
│       ├── main.jsx                        # Application root entry
│       ├── App.jsx                         # React Router route registry
│       ├── index.css                       # Tailwind styles & sleek scrollbars
│       ├── context/
│       │   └── AuthContext.jsx             # Global user, auth & settings state
│       ├── services/                       # Centralized REST API client
│       │   ├── api.js                      # Token-injecting fetch wrapper
│       │   ├── authService.js              # Login, register, session checks
│       │   ├── transactionService.js       # CRUD and multi-criteria search
│       │   ├── categoryService.js          # Categories & safety checks
│       │   ├── budgetService.js            # Budgets & threshold alerts
│       │   ├── reportService.js            # Metrics & trend analytics
│       │   ├── exportService.js            # Browser CSV file downloads
│       │   ├── settingsService.js          # Currency & date format preferences
│       │   └── sampleDataService.js        # Demo data seed & safe purge
│       ├── components/                     # Reusable UI widgets
│       │   ├── Navbar.jsx                  # Header with currency & user badges
│       │   ├── Sidebar.jsx                 # Dark navy (#0F172A) navigation
│       │   ├── MetricCard.jsx              # Colored KPI stat cards
│       │   ├── ProgressBar.jsx             # Adaptive threshold progress bar
│       │   ├── Modal.jsx                   # Accessible modal dialog
│       │   ├── ConfirmDialog.jsx           # Deletion confirmation modal
│       │   ├── DemoDataBanner.jsx          # Demo data status & purge banner
│       │   └── ProtectedRoute.jsx          # Route authentication guard
│       ├── layouts/
│       │   └── AppLayout.jsx               # Sticky sidebar + topbar shell
│       ├── pages/                          # The 10 Core Application Modules
│       │   ├── Login.jsx                   # Authentication with demo quick-fill
│       │   ├── Register.jsx                # New user registration
│       │   ├── Dashboard.jsx               # Module 1: Executive KPI Dashboard
│       │   ├── Transactions.jsx            # Module 2: Transaction Ledger & Edit
│       │   ├── AddTransaction.jsx          # Module 3: Fast Transaction Entry
│       │   ├── Categories.jsx              # Module 4: Category Management
│       │   ├── BudgetManagement.jsx        # Module 5: Budgets & 80%/100% Alerts
│       │   ├── ReportsAnalytics.jsx        # Module 6: Reports & Visual Analytics
│       │   ├── SearchFilters.jsx           # Module 7: Multi-Criteria Search
│       │   ├── ExportData.jsx              # Module 8: Excel-Safe CSV Exports
│       │   ├── Settings.jsx                # Module 9: System Settings & Purge
│       │   └── AboutProject.jsx            # Module 10: Academic Details & Viva Q&A
│       └── utils/
│           ├── constants.js                # Currencies, months, color palettes
│           └── formatters.js               # BigDecimal currency & date formatters
├── backend/                                # Spring Boot 3 REST API
│   ├── pom.xml                             # Maven build configuration
│   ├── Dockerfile                          # Multi-stage production container
│   ├── src/main/java/com/expense/tracker/
│   │   ├── PersonalExpenseTrackerWebApplication.java
│   │   ├── config/                         # Security & CORS configuration
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── CorsConfig.java
│   │   ├── model/                          # JPA Entities
│   │   │   ├── User.java                   # Users table
│   │   │   ├── Transaction.java            # Transactions table
│   │   │   ├── TransactionType.java        # INCOME / EXPENSE enum
│   │   │   ├── Category.java               # Categories table
│   │   │   ├── Budget.java                 # Budgets table
│   │   │   └── UserSettings.java           # User Settings table
│   │   ├── repository/                     # Spring Data JPA Repositories
│   │   ├── dto/                            # Data Transfer Objects
│   │   ├── service/                        # Business Logic Layer
│   │   ├── controller/                     # REST API Controllers
│   │   └── exception/                      # Centralized Exception Handling
│   └── src/main/resources/
│       ├── application.yml                 # Default & local configuration
│       └── application-prod.yml            # Production Neon PostgreSQL config
├── .env.example                            # Comprehensive environment variables template
├── .gitignore                              # Git exclusion patterns
├── render.yaml                             # Render Infrastructure-as-Code Blueprint
├── run-backend.bat                         # Windows backend launcher
├── run-frontend.bat                        # Windows frontend launcher
└── test-backend.bat                        # Backend automated test runner
```

---

## 🚀 How to Run Locally

### 1. Prerequisites
- **Java 17 or 21 LTS** installed (`java -version`).
- **Node.js 18+** installed (`node -v`).
- **Maven** (optional, wrapper/bundled tool available).

### 2. Start the Backend
From the project root:
```bash
cd PersonalExpenseTracker-Web/backend
mvn clean spring-boot:run
```
*Or on Windows, simply double-click `run-backend.bat`.*
- The backend will start on **`http://localhost:8080`**.
- It uses embedded H2 by default (no external database installation needed for local development).
- Health check: `http://localhost:8080/api/health`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/expense_web_db`, Username: `sa`, Password: empty)

### 3. Start the Frontend
From a new terminal:
```bash
cd PersonalExpenseTracker-Web/frontend
npm install
npm run dev
```
*Or on Windows, simply double-click `run-frontend.bat`.*
- The web app will open at **`http://localhost:5173`**.
- Vite dev server automatically proxies `/api` calls to `http://localhost:8080`.

---

## ☁️ Deployment Guide

### A. Database — Neon PostgreSQL (Free Tier)
1. Sign up at [neon.tech](https://neon.tech) and create a project named `expense-tracker`.
2. Copy the connection string details (host, database name, user, password, or full URL).
3. Ensure the JDBC URL format is:
   ```
   jdbc:postgresql://<neon-host>/<db-name>?sslmode=require
   ```

### B. Backend — Render.com (Free Web Service)
1. Sign up at [render.com](https://render.com) and click **New → Web Service**.
2. Connect your GitHub repository.
3. Configure the service:
   - **Root Directory:** `PersonalExpenseTracker-Web/backend`
   - **Environment:** Docker (uses the provided `backend/Dockerfile`)
   - **Plan:** Free
4. Add Environment Variables:
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `DATABASE_URL`: `jdbc:postgresql://<neon-host>/neondb?sslmode=require`
   - `DATABASE_USERNAME`: `<neon-username>`
   - `DATABASE_PASSWORD`: `<neon-password>`
   - `JWT_SECRET`: `<generate-a-random-64-character-hex-secret>`
   - `CORS_ALLOWED_ORIGINS`: `https://<your-vercel-app>.vercel.app,http://localhost:5173`
5. Click **Deploy**. Your API will be live at `https://<your-backend>.onrender.com`.

### C. Frontend — Vercel (Free Edge Hosting)
1. Sign up at [vercel.com](https://vercel.com) and click **Add New → Project**.
2. Import your GitHub repository.
3. Configure the project:
   - **Root Directory:** `PersonalExpenseTracker-Web/frontend`
   - **Framework Preset:** Vite
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`
4. Add Environment Variable:
   - `VITE_API_URL`: `https://<your-backend>.onrender.com/api`
5. Click **Deploy**. Your website will be live at `https://<your-app>.vercel.app`.

---

## 🔒 Security Highlights

- **BCrypt Password Encryption:** Passwords are never stored in plain text. Salted and hashed with BCrypt (10 rounds).
- **Stateless JWT Tokens:** Tokens expire in 24 hours and are signed with HMAC-SHA256 (`HS512`).
- **CORS Protection:** Cross-Origin Resource Sharing is locked down to authorized frontend domains.
- **SQL Injection Prevention:** Spring Data JPA / Hibernate parameterized queries prevent SQL injection.
- **User Isolation:** All database queries are filtered by `WHERE user_id = :userId`.

---

## 🎓 Academic Viva & Evaluation Notes

- **Student Name:** Ritika Kadian
- **Roll Number:** CS-2024-042
- **Topic:** Personal Expense Tracker Full-Stack Web Application
- **Key OOP Principles Covered:** Encapsulation (Entities & DTOs), Inheritance, Polymorphism, Abstraction (Service & Repository Interfaces).
- **Database Schema:** 3NF relational normalization with referential integrity constraints.
- **Safety Guards:** Cascading category rename, safe deletion validation (preventing orphan transactions), and isolated demo data purging.
