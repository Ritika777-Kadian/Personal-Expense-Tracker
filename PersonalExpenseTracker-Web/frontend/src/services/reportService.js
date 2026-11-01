import { apiRequest } from './api';

export const reportService = {
  getSummary: async (year, month) => {
    const query = new URLSearchParams();
    if (year) query.append('year', year);
    if (month) query.append('month', month);
    return apiRequest(`/reports/summary?${query.toString()}`);
  },

  getTrends: async (year) => {
    const query = new URLSearchParams();
    if (year) query.append('year', year);
    return apiRequest(`/reports/trends?${query.toString()}`);
  },

  getCategoryBreakdown: async (start, end) => {
    const query = new URLSearchParams();
    if (start) query.append('start', start);
    if (end) query.append('end', end);
    return apiRequest(`/reports/category-breakdown?${query.toString()}`);
  },

  getDashboardData: async (year, month) => {
    const query = new URLSearchParams();
    if (year) query.append('year', year);
    if (month) query.append('month', month);
    return apiRequest(`/dashboard/summary?${query.toString()}`);
  },
};
