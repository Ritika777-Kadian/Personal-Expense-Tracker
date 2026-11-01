import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Receipt,
  PlusCircle,
  FolderTree,
  PiggyBank,
  BarChart3,
  Search,
  Download,
  Settings,
  Info,
  Wallet,
  X,
} from 'lucide-react';

const NAV_ITEMS = [
  { name: 'Dashboard', path: '/', icon: LayoutDashboard },
  { name: 'Transactions', path: '/transactions', icon: Receipt },
  { name: 'Add Transaction', path: '/add-transaction', icon: PlusCircle },
  { name: 'Categories', path: '/categories', icon: FolderTree },
  { name: 'Budget & Alerts', path: '/budgets', icon: PiggyBank },
  { name: 'Reports & Analytics', path: '/reports', icon: BarChart3 },
  { name: 'Search & Filters', path: '/search', icon: Search },
  { name: 'Export Data', path: '/export', icon: Download },
  { name: 'Settings', path: '/settings', icon: Settings },
  { name: 'About Project', path: '/about', icon: Info },
];

export const Sidebar = ({ isOpen, onClose }) => {
  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/60 backdrop-blur-sm lg:hidden"
          onClick={onClose}
        />
      )}

      <aside
        className={`fixed top-0 bottom-0 left-0 z-50 w-64 bg-[#0F172A] text-slate-300 flex flex-col transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Logo / Header */}
        <div className="h-16 flex items-center justify-between px-6 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-teal-500 to-emerald-400 flex items-center justify-center text-white shadow-md shadow-teal-500/20">
              <Wallet className="w-5 h-5" />
            </div>
            <div>
              <h1 className="font-bold text-white text-base leading-tight tracking-tight">ExpenseTracker</h1>
              <span className="text-[10px] text-teal-400 font-semibold uppercase tracking-wider">Web Edition</span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="lg:hidden text-slate-400 hover:text-white p-1 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Items */}
        <div className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
          <div className="px-3 pb-2 text-[11px] font-semibold uppercase tracking-wider text-slate-500">
            Main Menu
          </div>
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => onClose && onClose()}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 ${
                    isActive
                      ? 'bg-teal-500/10 text-teal-400 border border-teal-500/20 font-semibold'
                      : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                <span>{item.name}</span>
              </NavLink>
            );
          })}
        </div>

        {/* Footer info */}
        <div className="p-4 border-t border-slate-800/80 bg-slate-950/40 text-[11px] text-slate-400">
          <div className="flex items-center justify-between">
            <span className="text-slate-500">Spring Boot + React</span>
            <span className="px-1.5 py-0.5 rounded bg-slate-800 text-teal-400 font-mono text-[10px]">v1.0.0</span>
          </div>
          <div className="mt-1 text-slate-400">Ritika Kadian &bull; CS-2024-042</div>
        </div>
      </aside>
    </>
  );
};
