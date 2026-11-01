import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AppLayout } from './layouts/AppLayout';

// Pages
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Dashboard } from './pages/Dashboard';
import { Transactions } from './pages/Transactions';
import { AddTransaction } from './pages/AddTransaction';
import { Categories } from './pages/Categories';
import { BudgetManagement } from './pages/BudgetManagement';
import { ReportsAnalytics } from './pages/ReportsAnalytics';
import { SearchFilters } from './pages/SearchFilters';
import { ExportData } from './pages/ExportData';
import { Settings } from './pages/Settings';
import { AboutProject } from './pages/AboutProject';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Application Routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/" element={<Dashboard />} />
              <Route path="/transactions" element={<Transactions />} />
              <Route path="/add-transaction" element={<AddTransaction />} />
              <Route path="/categories" element={<Categories />} />
              <Route path="/budgets" element={<BudgetManagement />} />
              <Route path="/reports" element={<ReportsAnalytics />} />
              <Route path="/search" element={<SearchFilters />} />
              <Route path="/export" element={<ExportData />} />
              <Route path="/settings" element={<Settings />} />
              <Route path="/about" element={<AboutProject />} />
            </Route>
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
