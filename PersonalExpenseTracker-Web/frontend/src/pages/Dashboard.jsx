import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { reportService } from '../services/reportService';
import { sampleDataService } from '../services/sampleDataService';
import { MetricCard } from '../components/MetricCard';
import { ProgressBar } from '../components/ProgressBar';
import { DemoDataBanner } from '../components/DemoDataBanner';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { formatCurrency, formatDate } from '../utils/formatters';
import { MONTH_NAMES } from '../utils/constants';
import {
  TrendingUp,
  TrendingDown,
  PiggyBank,
  Wallet,
  Calendar,
  AlertTriangle,
  PlusCircle,
  ArrowRight,
  Receipt,
  PieChart as PieIcon,
  BarChart3,
  Activity,
  Sparkles,
  Trash2,
  RefreshCw,
  CheckCircle2,
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
} from 'recharts';

export const Dashboard = () => {
  const { currencySymbol, settings, demoDataActive, refreshDemoStatus } = useAuth();
  const navigate = useNavigate();

  const currentDate = new Date();
  const [selectedYear, setSelectedYear] = useState(currentDate.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(currentDate.getMonth() + 1);

  const [loading, setLoading] = useState(true);
  const [dashboardData, setDashboardData] = useState(null);
  const [error, setError] = useState(null);

  // Demo Data Action states
  const [demoActionLoading, setDemoActionLoading] = useState(false);
  const [confirmLoadOpen, setConfirmLoadOpen] = useState(false);
  const [confirmClearOpen, setConfirmClearOpen] = useState(false);
  const [demoFeedback, setDemoFeedback] = useState(null);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await reportService.getDashboardData(selectedYear, selectedMonth);
      setDashboardData(data);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
      setError('Could not retrieve dashboard statistics. Ensure backend is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, [selectedYear, selectedMonth]);

  const handleLoadDemo = async () => {
    try {
      setDemoActionLoading(true);
      const res = await sampleDataService.seed(selectedYear, selectedMonth);
      await refreshDemoStatus();
      setConfirmLoadOpen(false);
      setDemoFeedback({
        type: res.success ? 'success' : 'info',
        text: res.message,
      });
      await fetchDashboard();
      setTimeout(() => setDemoFeedback(null), 6000);
    } catch (err) {
      console.error('Failed to load demo data', err);
      setDemoFeedback({
        type: 'error',
        text: err.message || 'Error occurred while loading demo data.',
      });
      setTimeout(() => setDemoFeedback(null), 6000);
    } finally {
      setDemoActionLoading(false);
    }
  };

  const handleClearDemo = async () => {
    try {
      setDemoActionLoading(true);
      const res = await sampleDataService.purge();
      await refreshDemoStatus();
      setConfirmClearOpen(false);
      setDemoFeedback({
        type: 'success',
        text: res.message,
      });
      await fetchDashboard();
      setTimeout(() => setDemoFeedback(null), 6000);
    } catch (err) {
      console.error('Failed to clear demo data', err);
      setDemoFeedback({
        type: 'error',
        text: err.message || 'Error occurred while clearing demo data.',
      });
      setTimeout(() => setDemoFeedback(null), 6000);
    } finally {
      setDemoActionLoading(false);
    }
  };

  const summary = dashboardData?.summary || {};
  const categories = dashboardData?.categoryBreakdown || [];
  const trends = dashboardData?.monthlyTrends || [];
  const recentTransactions = dashboardData?.recentTransactions || [];

  return (
    <div className="space-y-6">
      {/* Feedback Toast */}
      {demoFeedback && (
        <div
          className={`p-3.5 rounded-2xl text-xs font-semibold flex items-center justify-between gap-3 shadow-sm transition-all border ${
            demoFeedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
              : demoFeedback.type === 'error'
              ? 'bg-rose-50 text-rose-800 border-rose-200'
              : 'bg-amber-50 text-amber-800 border-amber-200'
          }`}
        >
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{demoFeedback.text}</span>
          </div>
          <button
            onClick={() => setDemoFeedback(null)}
            className="text-[11px] underline font-medium hover:opacity-80"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Demo Data Banner */}
      <DemoDataBanner
        onRefresh={fetchDashboard}
        selectedYear={selectedYear}
        selectedMonth={selectedMonth}
      />

      {/* Header, Month Selector and Action Buttons */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Financial Dashboard</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Real-time calculations for {MONTH_NAMES[selectedMonth - 1]} {selectedYear}
          </p>
        </div>

        {/* Date / Month Picker & Primary Actions */}
        <div className="flex items-center gap-2 self-stretch sm:self-auto flex-wrap">
          <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 px-3 py-1.5 rounded-xl shadow-inner text-xs font-semibold text-slate-700">
            <Calendar className="w-4 h-4 text-slate-400" />
            <select
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(Number(e.target.value))}
              className="bg-transparent focus:outline-none cursor-pointer"
            >
              {MONTH_NAMES.map((name, idx) => (
                <option key={idx + 1} value={idx + 1}>
                  {name}
                </option>
              ))}
            </select>
            <select
              value={selectedYear}
              onChange={(e) => setSelectedYear(Number(e.target.value))}
              className="bg-transparent focus:outline-none cursor-pointer border-l border-slate-200 pl-2"
            >
              {[2024, 2025, 2026, 2027].map((y) => (
                <option key={y} value={y}>
                  {y}
                </option>
              ))}
            </select>
          </div>

          {/* Load Demo Data / Clear Demo Data Button */}
          {!demoDataActive ? (
            <button
              onClick={() => setConfirmLoadOpen(true)}
              disabled={demoActionLoading}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 border border-indigo-200 text-xs font-bold rounded-xl shadow-sm transition-all"
              title="Load realistic sample data to preview all features and charts"
            >
              {demoActionLoading ? (
                <RefreshCw className="w-4 h-4 text-indigo-600 animate-spin" />
              ) : (
                <Sparkles className="w-4 h-4 text-indigo-600" />
              )}
              <span>{demoActionLoading ? 'Loading...' : 'Load Demo Data'}</span>
            </button>
          ) : (
            <button
              onClick={() => setConfirmClearOpen(true)}
              disabled={demoActionLoading}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 text-xs font-bold rounded-xl shadow-sm transition-all"
              title="Clear demo transactions while preserving your personal records"
            >
              {demoActionLoading ? (
                <RefreshCw className="w-4 h-4 text-rose-600 animate-spin" />
              ) : (
                <Trash2 className="w-4 h-4 text-rose-600" />
              )}
              <span>{demoActionLoading ? 'Clearing...' : 'Clear Demo Data'}</span>
            </button>
          )}

          <button
            onClick={() => navigate('/add-transaction')}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-teal-600 hover:bg-teal-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all"
          >
            <PlusCircle className="w-4 h-4" />
            <span className="hidden sm:inline">Add Transaction</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 text-xs">
          {error}
        </div>
      )}

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <MetricCard
          title="Monthly Income"
          value={formatCurrency(summary.monthlyIncome, currencySymbol)}
          subtitle={`${MONTH_NAMES[selectedMonth - 1]} Inflow`}
          icon={TrendingUp}
          variant="emerald"
        />
        <MetricCard
          title="Monthly Expenses"
          value={formatCurrency(summary.monthlyExpense, currencySymbol)}
          subtitle={`${MONTH_NAMES[selectedMonth - 1]} Outflow`}
          icon={TrendingDown}
          variant="rose"
        />
        <MetricCard
          title="Monthly Savings"
          value={formatCurrency(summary.monthlySavings, currencySymbol)}
          subtitle="Net Cashflow"
          icon={PiggyBank}
          variant="sky"
        />
        <MetricCard
          title="All-Time Balance"
          value={formatCurrency(summary.allTimeBalance, currencySymbol)}
          subtitle="Cumulative Balance"
          icon={Wallet}
          variant="indigo"
        />
        <MetricCard
          title="Monthly Budget"
          value={formatCurrency(summary.monthlyBudget, currencySymbol)}
          subtitle="Target Spending"
          icon={AlertTriangle}
          variant="amber"
        />
        <MetricCard
          title="Remaining Budget"
          value={formatCurrency(summary.remainingBudget, currencySymbol)}
          subtitle={`${summary.budgetUtilizationPercentage || 0}% Utilized`}
          icon={Activity}
          variant={summary.budgetUtilizationPercentage > 100 ? 'rose' : 'teal'}
        />
      </div>

      {/* Budget Utilization Health Progress Bar */}
      {summary.monthlyBudget > 0 && (
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm space-y-2">
          <div className="flex items-center justify-between text-xs font-bold">
            <span className="text-slate-700">Monthly Budget Progress</span>
            <span
              className={
                summary.budgetUtilizationPercentage > 100
                  ? 'text-rose-600'
                  : summary.budgetUtilizationPercentage >= 80
                  ? 'text-amber-600'
                  : 'text-emerald-600'
              }
            >
              {summary.budgetUtilizationPercentage > 100
                ? 'CRITICAL ALERT: EXCEEDED (>100%)'
                : summary.budgetUtilizationPercentage >= 80
                ? 'WARNING: APPROACHING LIMIT (>= 80%)'
                : 'NORMAL STATUS'}
            </span>
          </div>
          <ProgressBar percentage={summary.budgetUtilizationPercentage || 0} height="h-3" />
        </div>
      )}

      {/* Visual Analytics Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Income vs Expenses Comparative Bar Chart */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <BarChart3 className="w-5 h-5 text-teal-600" />
              <h3 className="font-bold text-slate-800 text-sm">Monthly Inflow vs Outflow</h3>
            </div>
            <span className="text-xs text-slate-400 font-medium">12-Month Overview</span>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={trends} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                <XAxis dataKey="monthName" tick={{ fontSize: 11, fill: '#64748B' }} />
                <YAxis tick={{ fontSize: 11, fill: '#64748B' }} />
                <Tooltip
                  formatter={(val) => formatCurrency(val, currencySymbol)}
                  contentStyle={{ backgroundColor: '#0F172A', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
                <Bar dataKey="income" name="Income" fill="#10B981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="expense" name="Expense" fill="#EF4444" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Expense Category Distribution Doughnut Chart */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <PieIcon className="w-5 h-5 text-teal-600" />
              <h3 className="font-bold text-slate-800 text-sm">Spending by Category</h3>
            </div>
            <span className="text-xs text-slate-400 font-medium">
              Top: {summary.highestSpendingCategory || 'None'}
            </span>
          </div>

          {categories.length === 0 ? (
            <div className="h-64 flex flex-col items-center justify-center text-slate-400 text-xs">
              <PieIcon className="w-10 h-10 stroke-1 mb-2 text-slate-300" />
              <span>No expense records found for this period.</span>
            </div>
          ) : (
            <div className="h-64 w-full flex items-center justify-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categories}
                    dataKey="amount"
                    nameKey="category"
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={85}
                    paddingAngle={3}
                  >
                    {categories.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color || '#3B82F6'} />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(val, name, item) => [
                      `${formatCurrency(val, currencySymbol)} (${item.payload.percentage}%)`,
                      name,
                    ]}
                    contentStyle={{ backgroundColor: '#0F172A', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                  />
                  <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>

      {/* Annual Net Savings Trend Line Chart */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-teal-600" />
            <h3 className="font-bold text-slate-800 text-sm">Annual Net Savings Progression ({selectedYear})</h3>
          </div>
          <span className="text-xs text-slate-400 font-medium">Inflow Minus Outflow</span>
        </div>

        <div className="h-60 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={trends} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
              <XAxis dataKey="monthName" tick={{ fontSize: 11, fill: '#64748B' }} />
              <YAxis tick={{ fontSize: 11, fill: '#64748B' }} />
              <Tooltip
                formatter={(val) => formatCurrency(val, currencySymbol)}
                contentStyle={{ backgroundColor: '#0F172A', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
              />
              <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
              <Line
                type="monotone"
                dataKey="savings"
                name="Net Savings"
                stroke="#0EA5E9"
                strokeWidth={3}
                dot={{ r: 4, fill: '#0EA5E9' }}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Recent Transactions Section */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Receipt className="w-5 h-5 text-teal-600" />
            <h3 className="font-bold text-slate-800 text-sm">Recent Transactions</h3>
          </div>
          <button
            onClick={() => navigate('/transactions')}
            className="flex items-center gap-1 text-xs font-semibold text-teal-600 hover:text-teal-700 transition-colors"
          >
            <span>View All</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {recentTransactions.length === 0 ? (
          <div className="py-10 px-4 text-center max-w-sm mx-auto">
            <div className="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center mx-auto mb-3">
              <Sparkles className="w-6 h-6" />
            </div>
            <h4 className="text-sm font-bold text-slate-800">No Transactions Found Yet</h4>
            <p className="text-xs text-slate-500 mt-1 mb-4 leading-relaxed">
              Start by recording your first personal income or expense, or load realistic demo data across multiple months to explore charts and reports.
            </p>
            <div className="flex items-center justify-center gap-2">
              {!demoDataActive && (
                <button
                  onClick={() => setConfirmLoadOpen(true)}
                  className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all"
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>Load Demo Data</span>
                </button>
              )}
              <button
                onClick={() => navigate('/add-transaction')}
                className="flex items-center gap-1.5 px-3.5 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-xl shadow-sm transition-all"
              >
                <PlusCircle className="w-3.5 h-3.5" />
                <span>Add Transaction</span>
              </button>
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-100 text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                  <th className="py-2.5 px-3">Date</th>
                  <th className="py-2.5 px-3">Type</th>
                  <th className="py-2.5 px-3">Category</th>
                  <th className="py-2.5 px-3">Description</th>
                  <th className="py-2.5 px-3 text-right">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {recentTransactions.map((t) => {
                  const isInc = t.type === 'INCOME';
                  return (
                    <tr key={t.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="py-3 px-3 text-slate-500 font-mono text-[11px]">
                        {formatDate(t.date, settings.dateFormat)}
                      </td>
                      <td className="py-3 px-3">
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider ${
                            isInc
                              ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                              : 'bg-rose-50 text-rose-700 border border-rose-200'
                          }`}
                        >
                          {t.type}
                        </span>
                      </td>
                      <td className="py-3 px-3 font-medium text-slate-800">{t.category}</td>
                      <td className="py-3 px-3 text-slate-600 max-w-xs truncate">
                        {t.isDemo && (
                          <span className="mr-1.5 px-1.5 py-0.5 rounded bg-amber-100 text-amber-800 text-[10px] font-semibold">
                            DEMO
                          </span>
                        )}
                        {t.description || '-'}
                      </td>
                      <td
                        className={`py-3 px-3 text-right font-bold font-mono text-sm ${
                          isInc ? 'text-emerald-600' : 'text-slate-900'
                        }`}
                      >
                        {isInc ? '+' : '-'}
                        {formatCurrency(t.amount, currencySymbol)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Confirmation Dialog to Load Demo Data */}
      <ConfirmDialog
        isOpen={confirmLoadOpen}
        onClose={() => setConfirmLoadOpen(false)}
        onConfirm={handleLoadDemo}
        title="Load Realistic Demo Data?"
        message="This will insert sample income and expense transactions (Salary, Food, Shopping, Travel, Education, Bills, Entertainment, etc.) and monthly budgets across multiple months to showcase dashboard charts, category breakdown, budget utilization, and reports. Your personal transactions will NOT be overwritten or affected."
        confirmText="Yes, Load Demo Data"
        cancelText="Cancel"
        danger={false}
        loading={demoActionLoading}
      />

      {/* Confirmation Dialog to Clear Demo Data */}
      <ConfirmDialog
        isOpen={confirmClearOpen}
        onClose={() => setConfirmClearOpen(false)}
        onConfirm={handleClearDemo}
        title="Clear All Demo Data?"
        message="Are you sure you want to remove all demo records? Only transactions marked as demo data will be deleted. All of your actual personal user transactions are completely preserved."
        confirmText="Clear Demo Data"
        cancelText="Cancel"
        danger={true}
        loading={demoActionLoading}
      />
    </div>
  );
};
