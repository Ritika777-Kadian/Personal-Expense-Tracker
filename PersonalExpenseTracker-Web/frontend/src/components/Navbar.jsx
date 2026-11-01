import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Menu, LogOut, User, Calendar, Database, Check } from 'lucide-react';
import { CURRENCIES } from '../utils/constants';

export const Navbar = ({ onOpenSidebar }) => {
  const { user, settings, currencySymbol, updateSettings, logout, demoDataActive } = useAuth();
  const [currencyDropdownOpen, setCurrencyDropdownOpen] = useState(false);

  const todayStr = new Date().toLocaleDateString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });

  const handleSelectCurrency = async (code) => {
    try {
      await updateSettings({ ...settings, currency: code });
      setCurrencyDropdownOpen(false);
    } catch (err) {
      console.error('Failed to change currency', err);
    }
  };

  return (
    <header className="sticky top-0 z-30 h-16 bg-white/80 backdrop-blur-md border-b border-slate-200/80 px-4 lg:px-8 flex items-center justify-between">
      {/* Left side: Mobile menu toggle + breadcrumb / date */}
      <div className="flex items-center gap-3">
        <button
          onClick={onOpenSidebar}
          className="lg:hidden p-2 rounded-xl text-slate-600 hover:bg-slate-100 transition-colors"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className="hidden sm:flex items-center gap-2 text-xs text-slate-500 font-medium bg-slate-100/80 px-3 py-1.5 rounded-full border border-slate-200/60">
          <Calendar className="w-3.5 h-3.5 text-slate-400" />
          <span>{todayStr}</span>
        </div>
      </div>

      {/* Right side: Demo badge, Currency Switcher, User profile & Logout */}
      <div className="flex items-center gap-3">
        {/* Demo Data badge */}
        {demoDataActive && (
          <div className="hidden md:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-amber-50 border border-amber-200 text-amber-700 text-xs font-semibold">
            <Database className="w-3.5 h-3.5 text-amber-600 animate-pulse" />
            <span>DEMO MODE</span>
          </div>
        )}

        {/* Currency Selector */}
        <div className="relative">
          <button
            onClick={() => setCurrencyDropdownOpen(!currencyDropdownOpen)}
            className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-xs font-semibold text-slate-700 shadow-sm transition-colors"
            title="Switch Currency"
          >
            <span className="text-teal-600 font-bold">{currencySymbol}</span>
            <span>{settings?.currency || 'INR'}</span>
          </button>

          {currencyDropdownOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-white rounded-2xl shadow-xl border border-slate-200 py-1.5 z-50">
              <div className="px-3 py-1.5 text-[11px] font-semibold text-slate-400 uppercase tracking-wider border-b border-slate-100">
                Select Currency
              </div>
              {CURRENCIES.map((c) => (
                <button
                  key={c.code}
                  onClick={() => handleSelectCurrency(c.code)}
                  className={`w-full text-left px-3.5 py-2 text-xs flex items-center justify-between hover:bg-slate-50 transition-colors ${
                    settings?.currency === c.code ? 'font-bold text-teal-600 bg-teal-50/50' : 'text-slate-700'
                  }`}
                >
                  <span className="flex items-center gap-2">
                    <span className="w-5 text-center font-bold">{c.symbol}</span>
                    <span>{c.name}</span>
                  </span>
                  {settings?.currency === c.code && <Check className="w-3.5 h-3.5 text-teal-600" />}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* User Pill */}
        <div className="flex items-center gap-2 pl-2 border-l border-slate-200">
          <div className="w-8 h-8 rounded-full bg-slate-900 text-white flex items-center justify-center text-xs font-bold uppercase shadow-sm">
            {user?.username?.charAt(0) || <User className="w-4 h-4" />}
          </div>
          <div className="hidden sm:block text-left">
            <p className="text-xs font-bold text-slate-800 leading-none">{user?.fullName || user?.username}</p>
            <p className="text-[10px] text-slate-400 leading-none mt-0.5">{user?.email}</p>
          </div>

          <button
            onClick={logout}
            className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition-colors ml-1"
            title="Sign Out"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
