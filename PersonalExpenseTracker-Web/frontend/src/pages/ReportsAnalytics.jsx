import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { reportService } from '../services/reportService';
import { exportService } from '../services/exportService';
import { formatCurrency, formatDate } from '../utils/formatters';
import {
  BarChart3,
  Calendar,
  Download,
  TrendingUp,
  TrendingDown,
  PiggyBank,
  PieChart as PieIcon,
  Percent,
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
} from 'recharts';

export const ReportsAnalytics = () => {
  const { currencySymbol, settings } = useAuth();

  const now = new Date();
  const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
  const lastDayOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0];

  const [startDate, setStartDate] = useState(firstDayOfMonth);
  const [endDate, setEndDate] = useState(lastDayOfMonth);

  const [categories, setCategories] = useState([]);
  const [trends, setTrends] = useState([]);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);

  const fetchReports = async () => {
    try {
      setLoading(true);
      const [catList, trendList] = await Promise.all([
        reportService.getCategoryBreakdown(startDate, endDate),
        reportService.getTrends(now.getFullYear()),
      ]);
      setCategories(catList || []);
      setTrends(trendList || []);
    } catch (err) {
      console.error('Failed to load reports', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, [startDate, endDate]);

  const totalExpense = categories.reduce((sum, c) => sum + parseFloat(c.amount || 0), 0);

  const handleExportReport = async () => {
    try {
      setExporting(true);
      const blob = await exportService.exportCategoryReport(startDate, endDate);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Category_Report_${startDate}_to_${endDate}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert('Failed to export report');
    } finally {
      setExporting(false);
    }
  };

  const setRangeQuick = (type) => {
    const today = new Date();
    if (type === 'THIS_MONTH') {
      setStartDate(new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0]);
      setEndDate(new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0]);
    } else if (type === 'LAST_MONTH') {
      setStartDate(new Date(today.getFullYear(), today.getMonth() - 1, 1).toISOString().split('T')[0]);
      setEndDate(new Date(today.getFullYear(), today.getMonth(), 0).toISOString().split('T')[0]);
    } else if (type === 'THIS_YEAR') {
      setStartDate(new Date(today.getFullYear(), 0, 1).toISOString().split('T')[0]);
      setEndDate(new Date(today.getFullYear(), 11, 31).toISOString().split('T')[0]);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Reports & Visual Analytics</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Evaluate categorical distribution, inflow/outflow trends, and date-range aggregations
          </p>
        </div>

        <button
          onClick={handleExportReport}
          disabled={exporting}
          className="flex items-center gap-1.5 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold rounded-xl shadow-sm transition-all"
        >
          <Download className="w-4 h-4 text-teal-400" />
          <span>{exporting ? 'Generating CSV...' : 'Export CSV Report'}</span>
        </button>
      </div>

      {/* Date Range Controls */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center gap-2 flex-wrap text-xs">
          <span className="font-bold text-slate-600">Period:</span>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-xl font-semibold text-slate-700"
          />
          <span className="text-slate-400">to</span>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-xl font-semibold text-slate-700"
          />
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <button
            onClick={() => setRangeQuick('THIS_MONTH')}
            className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-600 hover:bg-slate-50"
          >
            This Month
          </button>
          <button
            onClick={() => setRangeQuick('LAST_MONTH')}
            className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-600 hover:bg-slate-50"
          >
            Last Month
          </button>
          <button
            onClick={() => setRangeQuick('THIS_YEAR')}
            className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-600 hover:bg-slate-50"
          >
            Full Year
          </button>
        </div>
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Doughnut Chart */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-slate-800 text-sm">Expense Allocation</h3>
            <span className="text-xs text-slate-400">Total: {formatCurrency(totalExpense, currencySymbol)}</span>
          </div>

          {categories.length === 0 ? (
            <div className="h-64 flex items-center justify-center text-xs text-slate-400">
              No expenses in selected date range
            </div>
          ) : (
            <div className="h-64 w-full">
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

        {/* 12-Month Comparison */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-slate-800 text-sm">Annual Monthly Comparison</h3>
            <span className="text-xs text-slate-400">{now.getFullYear()}</span>
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
      </div>

      {/* Category Breakdown Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <h3 className="font-bold text-slate-800 text-sm">Detailed Category Summary</h3>
          <span className="text-xs text-slate-400 font-medium">Ranked by Expenditure</span>
        </div>

        {categories.length === 0 ? (
          <div className="py-8 text-center text-xs text-slate-400">
            No expenses found for the specified period.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/70 border-b border-slate-200/80 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3 px-4">Category</th>
                  <th className="py-3 px-4 text-center">Transactions</th>
                  <th className="py-3 px-4 text-right">Expenditure</th>
                  <th className="py-3 px-4 text-right">Share (%)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {categories.map((c) => (
                  <tr key={c.category} className="hover:bg-slate-50/60 transition-colors">
                    <td className="py-3.5 px-4 font-semibold text-slate-800 flex items-center gap-2">
                      <div className="w-3 h-3 rounded-full" style={{ backgroundColor: c.color }} />
                      <span>{c.category}</span>
                    </td>
                    <td className="py-3.5 px-4 text-center font-mono text-slate-600">{c.count}</td>
                    <td className="py-3.5 px-4 text-right font-bold font-mono text-slate-900">
                      {formatCurrency(c.amount, currencySymbol)}
                    </td>
                    <td className="py-3.5 px-4 text-right font-bold text-slate-700">
                      {c.percentage}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
