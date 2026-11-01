import React, { useState } from 'react';
import { exportService } from '../services/exportService';
import { Download, FileSpreadsheet, Calendar, CheckCircle2, ShieldCheck, AlertCircle } from 'lucide-react';

export const ExportData = () => {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [loadingTxn, setLoadingTxn] = useState(false);
  const [loadingCat, setLoadingCat] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  const triggerDownload = (blob, filename) => {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  };

  const handleExportTransactions = async () => {
    try {
      setLoadingTxn(true);
      setSuccessMsg('');
      const blob = await exportService.exportTransactions(startDate || undefined, endDate || undefined);
      const filename = `Transactions_Export_${new Date().toISOString().split('T')[0]}.csv`;
      triggerDownload(blob, filename);
      setSuccessMsg(`Successfully generated ${filename}`);
      setTimeout(() => setSuccessMsg(''), 4000);
    } catch (err) {
      alert('Failed to export transactions');
    } finally {
      setLoadingTxn(false);
    }
  };

  const handleExportCategoryReport = async () => {
    try {
      setLoadingCat(true);
      setSuccessMsg('');
      const blob = await exportService.exportCategoryReport(startDate || undefined, endDate || undefined);
      const filename = `Category_Report_${new Date().toISOString().split('T')[0]}.csv`;
      triggerDownload(blob, filename);
      setSuccessMsg(`Successfully generated ${filename}`);
      setTimeout(() => setSuccessMsg(''), 4000);
    } catch (err) {
      alert('Failed to export category report');
    } finally {
      setLoadingCat(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Top Header */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm">
        <h2 className="text-xl font-bold text-slate-900 tracking-tight">Data Export Center</h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Generate Microsoft Excel-compatible CSV exports with RFC-4180 escaping and UTF-8 Byte Order Mark (BOM)
        </p>
      </div>

      {successMsg && (
        <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold flex items-center gap-2">
          <CheckCircle2 className="w-5 h-5 text-emerald-600" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Date Filter Card (Optional) */}
      <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm space-y-3">
        <h3 className="font-bold text-slate-800 text-sm flex items-center gap-2">
          <Calendar className="w-4 h-4 text-teal-600" />
          <span>Optional Date Interval Filtering</span>
        </h3>
        <p className="text-xs text-slate-500">
          Leave fields blank to export all-time financial records, or restrict output to a specific date range:
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
          <div>
            <label className="block text-xs font-bold text-slate-600 uppercase mb-1">Start Date</label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-600 uppercase mb-1">End Date</label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700"
            />
          </div>
        </div>
      </div>

      {/* Export Options Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Option 1: Full Transactions CSV */}
        <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm flex flex-col justify-between space-y-4">
          <div>
            <div className="w-12 h-12 rounded-2xl bg-teal-50 text-teal-600 flex items-center justify-center mb-3">
              <FileSpreadsheet className="w-6 h-6" />
            </div>
            <h4 className="font-bold text-slate-900 text-base">Transactions Ledger CSV</h4>
            <p className="text-xs text-slate-500 mt-1 leading-relaxed">
              Exports the entire transaction ledger with ID, Type (INCOME/EXPENSE), Amount, Category, Description, and Date. Encoded with UTF-8 BOM for direct double-click opening in Microsoft Excel.
            </p>
          </div>

          <button
            onClick={handleExportTransactions}
            disabled={loadingTxn}
            className="w-full py-3 bg-teal-600 hover:bg-teal-700 text-white rounded-xl text-xs font-bold shadow-md shadow-teal-600/20 flex items-center justify-center gap-2 transition-all"
          >
            <Download className="w-4 h-4" />
            <span>{loadingTxn ? 'Generating...' : 'Download Transactions CSV'}</span>
          </button>
        </div>

        {/* Option 2: Category Breakdown Report CSV */}
        <div className="bg-white p-6 rounded-3xl border border-slate-200/80 shadow-sm flex flex-col justify-between space-y-4">
          <div>
            <div className="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center mb-3">
              <FileSpreadsheet className="w-6 h-6" />
            </div>
            <h4 className="font-bold text-slate-900 text-base">Category Expense Report CSV</h4>
            <p className="text-xs text-slate-500 mt-1 leading-relaxed">
              Generates an executive analytical summary detailing category expenditures, relative percentage share of total outflow, and total transaction frequencies.
            </p>
          </div>

          <button
            onClick={handleExportCategoryReport}
            disabled={loadingCat}
            className="w-full py-3 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-bold shadow-md shadow-slate-900/20 flex items-center justify-center gap-2 transition-all"
          >
            <Download className="w-4 h-4 text-teal-400" />
            <span>{loadingCat ? 'Generating...' : 'Download Analytics CSV'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
