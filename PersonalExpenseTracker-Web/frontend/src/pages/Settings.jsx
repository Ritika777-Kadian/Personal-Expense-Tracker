import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { sampleDataService } from '../services/sampleDataService';
import { CURRENCIES } from '../utils/constants';
import {
  Settings as SettingsIcon,
  User,
  DollarSign,
  Calendar,
  Database,
  Trash2,
  RotateCcw,
  CheckCircle2,
  AlertCircle,
  LogOut,
  ShieldCheck,
} from 'lucide-react';

export const Settings = () => {
  const { user, settings, updateSettings, demoDataActive, refreshDemoStatus, logout } = useAuth();

  const [currency, setCurrency] = useState(settings?.currency || 'INR');
  const [dateFormat, setDateFormat] = useState(settings?.dateFormat || 'YYYY-MM-DD');
  const [savingSettings, setSavingSettings] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  const [demoActionLoading, setDemoActionLoading] = useState(false);
  const [demoMsg, setDemoMsg] = useState('');

  const handleSavePreferences = async (e) => {
    e.preventDefault();
    try {
      setSavingSettings(true);
      await updateSettings({
        ...settings,
        currency,
        dateFormat,
      });
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err) {
      alert('Failed to update settings');
    } finally {
      setSavingSettings(false);
    }
  };

  const handleLoadSampleData = async () => {
    try {
      setDemoActionLoading(true);
      setDemoMsg('');
      const res = await sampleDataService.seed();
      await refreshDemoStatus();
      setDemoMsg(res.message);
      setTimeout(() => setDemoMsg(''), 5000);
    } catch (err) {
      alert(err.message || 'Failed to load sample data');
    } finally {
      setDemoActionLoading(false);
    }
  };

  const handlePurgeSampleData = async () => {
    if (!window.confirm('Are you sure you want to purge all demo data? All personal transactions will be preserved.')) {
      return;
    }
    try {
      setDemoActionLoading(true);
      setDemoMsg('');
      const res = await sampleDataService.purge();
      await refreshDemoStatus();
      setDemoMsg(res.message);
      setTimeout(() => setDemoMsg(''), 5000);
    } catch (err) {
      alert(err.message || 'Failed to purge demo data');
    } finally {
      setDemoActionLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Top Header */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
        <h2 className="text-xl font-bold text-slate-900 tracking-tight">System Settings & Preferences</h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Configure financial display formats, user preferences, and demo datasets
        </p>
      </div>

      {saveSuccess && (
        <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold flex items-center gap-2">
          <CheckCircle2 className="w-5 h-5 text-emerald-600" />
          <span>Preferences updated successfully!</span>
        </div>
      )}

      {/* Profile Summary Card */}
      <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm flex items-center justify-between">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-slate-900 text-teal-400 flex items-center justify-center text-xl font-bold uppercase shadow-sm">
            {user?.username?.charAt(0) || <User className="w-7 h-7" />}
          </div>
          <div>
            <h3 className="text-base font-bold text-slate-900">{user?.fullName || user?.username}</h3>
            <p className="text-xs text-slate-500 font-mono">@{user?.username} &bull; {user?.email}</p>
            <span className="inline-block mt-1 text-[10px] font-bold uppercase px-2 py-0.5 rounded-full bg-teal-50 text-teal-700 border border-teal-200">
              Authenticated SaaS User
            </span>
          </div>
        </div>

        <button
          onClick={logout}
          className="px-4 py-2 rounded-xl border border-rose-200 bg-rose-50 text-rose-700 hover:bg-rose-100 text-xs font-bold flex items-center gap-1.5 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>

      {/* Financial Preferences Form */}
      <form onSubmit={handleSavePreferences} className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200/80 shadow-sm space-y-6">
        <h3 className="font-bold text-slate-800 text-sm flex items-center gap-2 border-b border-slate-100 pb-3">
          <DollarSign className="w-4 h-4 text-teal-600" />
          <span>Localization & Currency Display</span>
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div>
            <label className="block text-xs font-bold text-slate-600 uppercase mb-2">
              Default Currency Symbol
            </label>
            <select
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-teal-500"
            >
              {CURRENCIES.map((c) => (
                <option key={c.code} value={c.code}>
                  {c.name} ({c.symbol})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-600 uppercase mb-2">
              Temporal Date Format
            </label>
            <select
              value={dateFormat}
              onChange={(e) => setDateFormat(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-teal-500"
            >
              <option value="YYYY-MM-DD">YYYY-MM-DD (ISO Standard e.g. 2026-11-02)</option>
              <option value="DD/MM/YYYY">DD/MM/YYYY (UK / India e.g. 02/11/2026)</option>
              <option value="MM/DD/YYYY">MM/DD/YYYY (US Format e.g. 11/02/2026)</option>
            </select>
          </div>
        </div>

        <div className="flex justify-end pt-2">
          <button
            type="submit"
            disabled={savingSettings}
            className="px-6 py-2.5 bg-teal-600 hover:bg-teal-700 text-white rounded-xl text-xs font-bold shadow-md shadow-teal-600/20 transition-all"
          >
            {savingSettings ? 'Saving...' : 'Save Preferences'}
          </button>
        </div>
      </form>

      {/* Sample Demo Data Management Card */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200/80 shadow-sm space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div className="flex items-center gap-2">
            <Database className="w-5 h-5 text-amber-500" />
            <h3 className="font-bold text-slate-800 text-sm">Sample Demonstration Data Management</h3>
          </div>
          <span
            className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
              demoDataActive
                ? 'bg-amber-50 text-amber-700 border-amber-300'
                : 'bg-slate-100 text-slate-600 border-slate-200'
            }`}
          >
            {demoDataActive ? 'Active (20 Records)' : 'Inactive / Empty'}
          </span>
        </div>

        <p className="text-xs text-slate-500 leading-relaxed">
          The sample data service populates 20 realistic transactions across all 10 categories (Salary, Food, Bills, Rent, Healthcare, etc.) along with tiered monthly and category budgets. All demo records are tagged with <span className="font-mono text-amber-700">[DEMO DATA]</span>. Purging demo records removes strictly the demonstration entries and preserves 100% of your personal transactions.
        </p>

        {demoMsg && (
          <div className="p-3 bg-teal-50 border border-teal-200 text-teal-800 text-xs rounded-xl flex items-center gap-2 font-medium">
            <CheckCircle2 className="w-4 h-4 text-teal-600" />
            <span>{demoMsg}</span>
          </div>
        )}

        <div className="flex items-center gap-3 pt-2 flex-wrap">
          <button
            type="button"
            onClick={handleLoadSampleData}
            disabled={demoActionLoading || demoDataActive}
            className="px-4 py-2.5 bg-slate-900 hover:bg-slate-800 text-white disabled:opacity-40 rounded-xl text-xs font-bold flex items-center gap-2 transition-all"
          >
            <RotateCcw className="w-4 h-4 text-teal-400" />
            <span>Restore / Load 20 Sample Records</span>
          </button>

          <button
            type="button"
            onClick={handlePurgeSampleData}
            disabled={demoActionLoading || !demoDataActive}
            className="px-4 py-2.5 bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 disabled:opacity-40 rounded-xl text-xs font-bold flex items-center gap-2 transition-all"
          >
            <Trash2 className="w-4 h-4" />
            <span>Purge All Sample Records</span>
          </button>
        </div>
      </div>
    </div>
  );
};
