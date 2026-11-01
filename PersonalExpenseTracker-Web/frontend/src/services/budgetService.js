import { apiRequest } from './api';

export const budgetService = {
  getAll: async (year, month) => {
    const query = new URLSearchParams();
    if (year) query.append('year', year);
    if (month) query.append('month', month);
    return apiRequest(`/budgets?${query.toString()}`);
  },

  setBudget: async (budgetData) => {
    return apiRequest('/budgets', {
      method: 'POST',
      body: budgetData,
    });
  },

  delete: async (id) => {
    return apiRequest(`/budgets/${id}`, {
      method: 'DELETE',
    });
  },
};
