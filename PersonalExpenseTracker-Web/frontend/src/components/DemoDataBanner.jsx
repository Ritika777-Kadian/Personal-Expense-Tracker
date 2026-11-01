import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { sampleDataService } from '../services/sampleDataService';
import { ConfirmDialog } from './ConfirmDialog';
import { Sparkles, Trash2, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';

export const DemoDataBanner = ({ onRefresh, selectedYear, selectedMonth }) => {
  const { demoDataActive, refreshDemoStatus } = useAuth();
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState(null);
  const [confirmLoadOpen, setConfirmLoadOpen] = useState(false);
  const [confirmClearOpen, setConfirmClearOpen] = useState(false);

  const handleLoadDemo = async () => {
    try {
      setLoading(true);
      const res = await sampleDataService.seed(selectedYear, selectedMonth);
      await refreshDemoStatus();
      setConfirmLoadOpen(false);
      setMsg({ type: res.success ? 'success' : 'info', text: res.message });
      if (onRefresh) onRefresh();
      setTimeout(() => setMsg(null), 6000);
    } catch (err) {
      console.error('Failed to load demo data', err);
      setMsg({ type: 'error', text: err.message || 'Failed to load demo data' });
      setTimeout(() => setMsg(null), 6000);
    } finally {
      setLoading(false);
    }
  };

  const handleClearDemo = async () => {
    try {
      setLoading(true);
      const res = await sampleDataService.purge();
      await refreshDemoStatus();
      setConfirmClearOpen(false);
      setMsg({ type: 'success', text: res.message });
      if (onRefresh) onRefresh();
      setTimeout(() => setMsg(null), 6000);
    } catch (err) {
      console.error('Failed to clear demo data', err);
      setMsg({ type: 'error', text: err.message || 'Failed to clear demo data' });
      setTimeout(() => setMsg(null), 6000);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {/* Toast Notification message */}
      {msg && (
        <div
          className={`p-3.5 rounded-2xl text-xs font-semibold flex items-center justify-between gap-3 shadow-sm transition-all border ${
            msg.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
              : msg.type === 'error'
              ? 'bg-rose-50 text-rose-800 border-rose-200'
              : 'bg-amber-50 text-amber-800 border-amber-200'
          }`}
        >
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{msg.text}</span>
          </div>
          <button
            onClick={() => setMsg(null)}
            className="text-[11px] underline font-medium hover:opacity-80"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* When Demo Data is Active */}
      {demoDataActive ? (
        <div className="rounded-2xl border border-amber-300 bg-amber-50/90 p-4 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-amber-900 transition-all">
          <div className="flex items-start sm:items-center gap-3">
            <div className="p-2 bg-amber-100 rounded-xl text-amber-700 shrink-0">
              <AlertCircle className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-amber-900 flex items-center gap-2 flex-wrap">
                Demo Data Active
                <span className="text-[10px] font-semibold uppercase px-2 py-0.5 rounded-full bg-amber-200 text-amber-800">
                  Multi-Month Records & Budgets
                </span>
              </h4>
              <p className="text-xs text-amber-800 mt-0.5 leading-relaxed">
                Realistic sample transactions (Salary, Food, Shopping, Travel, Education, Bills, Entertainment) and budgets are populated across multiple months. Your personal transactions are completely safe.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 self-end sm:self-center shrink-0">
            <button
              onClick={() => setConfirmClearOpen(true)}
              disabled={loading}
              className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-bold bg-white text-rose-700 border border-rose-300 hover:bg-rose-50 shadow-sm transition-all"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>{loading ? 'Clearing...' : 'Clear Demo Data'}</span>
            </button>
          </div>
        </div>
      ) : (
        /* Subtle Invitation Banner when Demo Data is Not Loaded */
        <div className="rounded-2xl border border-indigo-100 bg-gradient-to-r from-indigo-50/70 via-white to-teal-50/40 p-4 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-slate-700">
          <div className="flex items-start sm:items-center gap-3">
            <div className="p-2 bg-indigo-100 rounded-xl text-indigo-700 shrink-0">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-slate-800 flex items-center gap-2">
                Explore with Realistic Demo Data
                <span className="text-[10px] font-semibold uppercase px-2 py-0.5 rounded-full bg-indigo-100 text-indigo-800">
                  Safe Demo Mode
                </span>
              </h4>
              <p className="text-xs text-slate-500 mt-0.5 leading-relaxed">
                Want to preview analytics, monthly trends, charts, and budget alerts? Load realistic demo transactions across Salary, Food, Shopping, Travel, Education, Bills, and Entertainment across 3 months.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 self-end sm:self-center shrink-0">
            <button
              onClick={() => setConfirmLoadOpen(true)}
              disabled={loading}
              className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold bg-indigo-600 hover:bg-indigo-700 text-white shadow-sm transition-all hover:shadow"
            >
              {loading ? (
                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <Sparkles className="w-3.5 h-3.5" />
              )}
              <span>{loading ? 'Loading...' : 'Load Demo Data'}</span>
            </button>
          </div>
        </div>
      )}

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
        loading={loading}
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
        loading={loading}
      />
    </>
  );
};
