import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { budgetService } from '../services/budgetService';
import { categoryService } from '../services/categoryService';
import { ProgressBar } from '../components/ProgressBar';
import { Modal } from '../components/Modal';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { formatCurrency } from '../utils/formatters';
import { MONTH_NAMES } from '../utils/constants';
import {
  PiggyBank,
  PlusCircle,
  Calendar,
  AlertTriangle,
  CheckCircle,
  Trash2,
  AlertCircle,
  TrendingUp,
} from 'lucide-react';

export const BudgetManagement = () => {
  const { currencySymbol } = useAuth();

  const currentDate = new Date();
  const [selectedYear, setSelectedYear] = useState(currentDate.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(currentDate.getMonth() + 1);

  const [budgets, setBudgets] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState('Overall');
  const [limit, setLimit] = useState('');
  const [modalError, setModalError] = useState('');

  // Delete State
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  const fetchBudgets = async () => {
    try {
      setLoading(true);
      const [bList, cList] = await Promise.all([
        budgetService.getAll(selectedYear, selectedMonth),
        categoryService.getAll('EXPENSE'),
      ]);
      setBudgets(bList || []);
      setCategories(cList || []);
    } catch (err) {
      console.error('Failed to load budgets', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBudgets();
  }, [selectedYear, selectedMonth]);

  const overallBudget = budgets.find((b) => b.category.toLowerCase() === 'overall');
  const categoryBudgets = budgets.filter((b) => b.category.toLowerCase() !== 'overall');

  const handleOpenAddModal = (presetCategory = 'Overall', presetLimit = '') => {
    setSelectedCategory(presetCategory);
    setLimit(presetLimit ? String(presetLimit) : '');
    setModalError('');
    setModalOpen(true);
  };

  const handleSaveBudget = async (e) => {
    e.preventDefault();
    if (!limit || parseFloat(limit) <= 0) {
      setModalError('Budget limit must be greater than zero');
      return;
    }

    try {
      setModalError('');
      await budgetService.setBudget({
        category: selectedCategory,
        monthlyLimit: parseFloat(limit),
        year: selectedYear,
        month: selectedMonth,
      });
      setModalOpen(false);
      fetchBudgets();
    } catch (err) {
      setModalError(err.message || 'Failed to set budget');
    }
  };

  const openDeleteDialog = (id) => {
    setDeletingId(id);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    try {
      await budgetService.delete(deletingId);
      setDeleteDialogOpen(false);
      fetchBudgets();
    } catch (err) {
      alert(err.message || 'Failed to delete budget');
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header with Month Selector */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">Budget & Threshold Alerts</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Set spending limits with automated 80% warning and 100% exceeded alerts
          </p>
        </div>

        <div className="flex items-center gap-2 self-stretch sm:self-auto">
          <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 px-3 py-1.5 rounded-xl text-xs font-semibold text-slate-700">
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

          <button
            onClick={() => handleOpenAddModal('Overall')}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-teal-600 hover:bg-teal-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Configure Budget</span>
          </button>
        </div>
      </div>

      {/* Overall Monthly Budget Card */}
      {overallBudget ? (
        <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-4">
            <div>
              <div className="flex items-center gap-2">
                <PiggyBank className="w-5 h-5 text-teal-600" />
                <h3 className="font-bold text-slate-900 text-base">
                  Overall Monthly Budget ({MONTH_NAMES[selectedMonth - 1]} {selectedYear})
                </h3>
              </div>
              <p className="text-xs text-slate-500 mt-1">
                Limit: <span className="font-bold font-mono text-slate-800">{formatCurrency(overallBudget.limit, currencySymbol)}</span> &bull; Spent: <span className="font-bold font-mono text-rose-600">{formatCurrency(overallBudget.spent, currencySymbol)}</span> &bull; Remaining: <span className="font-bold font-mono text-emerald-600">{formatCurrency(overallBudget.remaining, currencySymbol)}</span>
              </p>
            </div>

            <div className="flex items-center gap-2">
              <span
                className={`px-3 py-1 rounded-full text-xs font-bold border ${
                  overallBudget.exceeded
                    ? 'bg-rose-50 text-rose-700 border-rose-300'
                    : overallBudget.warning
                    ? 'bg-amber-50 text-amber-700 border-amber-300'
                    : 'bg-emerald-50 text-emerald-700 border-emerald-300'
                }`}
              >
                {overallBudget.exceeded
                  ? 'EXCEEDED (>100%)'
                  : overallBudget.warning
                  ? 'WARNING (≥80%)'
                  : 'NORMAL'}
              </span>

              <button
                onClick={() => handleOpenAddModal('Overall', overallBudget.limit)}
                className="px-3 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-lg"
              >
                Edit
              </button>
              <button
                onClick={() => openDeleteDialog(overallBudget.id)}
                className="p-1 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>

          <ProgressBar percentage={overallBudget.utilizationPercentage} height="h-3.5" showLabel={true} />
        </div>
      ) : (
        <div className="bg-white p-6 rounded-3xl border border-dashed border-slate-300 text-center">
          <PiggyBank className="w-10 h-10 text-slate-300 mx-auto mb-2" />
          <h4 className="font-bold text-slate-700 text-sm">No Overall Budget Configured</h4>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            Set an overall monthly limit to trigger proactive warnings before overspending.
          </p>
          <button
            onClick={() => handleOpenAddModal('Overall')}
            className="mt-4 px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-xl text-xs font-bold"
          >
            Set Overall Limit
          </button>
        </div>
      )}

      {/* Category-Wise Budgets Grid */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-bold text-slate-800 text-sm">Category-Wise Budgets</h3>
          <span className="text-xs text-slate-400 font-medium">
            {categoryBudgets.length} Category Target(s)
          </span>
        </div>

        {categoryBudgets.length === 0 ? (
          <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center text-xs text-slate-400">
            No category-specific budgets have been configured for this month.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {categoryBudgets.map((b) => (
              <div
                key={b.id}
                className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm space-y-3"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-teal-500" />
                    <h4 className="font-bold text-slate-800 text-sm">{b.category}</h4>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <span
                      className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                        b.exceeded
                          ? 'bg-rose-50 text-rose-700 border-rose-200'
                          : b.warning
                          ? 'bg-amber-50 text-amber-700 border-amber-200'
                          : 'bg-emerald-50 text-emerald-700 border-emerald-200'
                      }`}
                    >
                      {b.exceeded ? 'EXCEEDED' : b.warning ? 'WARNING' : 'NORMAL'}
                    </span>
                    <button
                      onClick={() => openDeleteDialog(b.id)}
                      className="p-1 text-slate-400 hover:text-rose-600 rounded"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between text-xs text-slate-500">
                  <span>Spent: <b className="font-mono text-slate-800">{formatCurrency(b.spent, currencySymbol)}</b></span>
                  <span>Limit: <b className="font-mono text-slate-800">{formatCurrency(b.limit, currencySymbol)}</b></span>
                  <span>Remaining: <b className={`font-mono ${b.remaining < 0 ? 'text-rose-600' : 'text-emerald-600'}`}>{formatCurrency(b.remaining, currencySymbol)}</b></span>
                </div>

                <ProgressBar percentage={b.utilizationPercentage} height="h-2.5" />
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add / Edit Budget Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Configure Monthly Budget">
        <form onSubmit={handleSaveBudget} className="space-y-4">
          {modalError && (
            <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{modalError}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase mb-1">Target Category *</label>
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-teal-500 cursor-pointer"
            >
              <option value="Overall">Overall (Total Monthly Budget)</option>
              {categories.map((c) => (
                <option key={c.id} value={c.name}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase mb-1">Monthly Spending Limit ({currencySymbol}) *</label>
            <input
              type="number"
              step="0.01"
              min="0.01"
              value={limit}
              onChange={(e) => setLimit(e.target.value)}
              placeholder="e.g. 15000.00"
              required
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-teal-500"
            />
          </div>

          <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-4 py-2 border border-slate-200 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-xl text-xs font-bold shadow-sm"
            >
              Save Budget
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={confirmDelete}
        title="Delete Budget"
        message="Are you sure you want to remove this budget limit?"
        confirmText="Remove Budget"
      />
    </div>
  );
};
