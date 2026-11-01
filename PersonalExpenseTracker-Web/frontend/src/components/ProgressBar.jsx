import React from 'react';

export const ProgressBar = ({ percentage, height = 'h-2.5', showLabel = false }) => {
  const clamped = Math.max(0, Math.min(percentage, 100));

  let barColor = 'bg-emerald-500';
  let badgeColor = 'bg-emerald-50 text-emerald-700 border-emerald-200';
  let statusText = 'Normal';

  if (percentage > 100) {
    barColor = 'bg-rose-500';
    badgeColor = 'bg-rose-50 text-rose-700 border-rose-200';
    statusText = 'Exceeded';
  } else if (percentage >= 80) {
    barColor = 'bg-amber-500';
    badgeColor = 'bg-amber-50 text-amber-700 border-amber-200';
    statusText = 'Warning (≥80%)';
  }

  return (
    <div className="w-full">
      {showLabel && (
        <div className="flex justify-between items-center text-xs mb-1.5 font-medium">
          <span className="text-slate-600">Utilization: {percentage.toFixed(1)}%</span>
          <span className={`px-2 py-0.5 rounded-full text-[11px] font-semibold border ${badgeColor}`}>
            {statusText}
          </span>
        </div>
      )}
      <div className={`w-full bg-slate-100 rounded-full overflow-hidden ${height}`}>
        <div
          className={`${height} ${barColor} transition-all duration-500 ease-out rounded-full`}
          style={{ width: `${clamped}%` }}
        />
      </div>
    </div>
  );
};
