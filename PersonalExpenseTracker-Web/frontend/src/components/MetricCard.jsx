import React from 'react';

export const MetricCard = ({ title, value, subtitle, icon: Icon, trend, variant = 'default' }) => {
  const getVariantStyles = () => {
    switch (variant) {
      case 'emerald':
        return {
          bg: 'bg-emerald-50/70 border-emerald-200/80',
          iconBg: 'bg-emerald-500 text-white',
          text: 'text-emerald-700',
        };
      case 'rose':
        return {
          bg: 'bg-rose-50/70 border-rose-200/80',
          iconBg: 'bg-rose-500 text-white',
          text: 'text-rose-700',
        };
      case 'teal':
        return {
          bg: 'bg-teal-50/70 border-teal-200/80',
          iconBg: 'bg-teal-600 text-white',
          text: 'text-teal-700',
        };
      case 'indigo':
        return {
          bg: 'bg-indigo-50/70 border-indigo-200/80',
          iconBg: 'bg-indigo-600 text-white',
          text: 'text-indigo-700',
        };
      case 'amber':
        return {
          bg: 'bg-amber-50/70 border-amber-200/80',
          iconBg: 'bg-amber-500 text-white',
          text: 'text-amber-700',
        };
      default:
        return {
          bg: 'bg-white border-slate-200/80',
          iconBg: 'bg-slate-900 text-white',
          text: 'text-slate-900',
        };
    }
  };

  const styles = getVariantStyles();

  return (
    <div className={`p-5 rounded-2xl border shadow-sm ${styles.bg} card-hover transition-all duration-200`}>
      <div className="flex items-center justify-between">
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">{title}</p>
        {Icon && (
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center shadow-sm ${styles.iconBg}`}>
            <Icon className="w-5 h-5" />
          </div>
        )}
      </div>

      <div className="mt-3">
        <h3 className={`text-2xl font-bold tracking-tight ${styles.text}`}>
          {value}
        </h3>
        {subtitle && (
          <p className="mt-1 text-xs text-slate-500 flex items-center gap-1">
            {subtitle}
          </p>
        )}
      </div>
    </div>
  );
};
