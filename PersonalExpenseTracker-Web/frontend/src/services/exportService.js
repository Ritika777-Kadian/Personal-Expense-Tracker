import { apiRequest } from './api';

export const exportService = {
  exportTransactions: async (start, end) => {
    const query = new URLSearchParams();
    if (start) query.append('start', start);
    if (end) query.append('end', end);
    return apiRequest(`/export/transactions/csv?${query.toString()}`);
  },

  exportCategoryReport: async (start, end) => {
    const query = new URLSearchParams();
    if (start) query.append('start', start);
    if (end) query.append('end', end);
    return apiRequest(`/export/category-report/csv?${query.toString()}`);
  },
};
