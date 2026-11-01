import { apiRequest } from './api';

export const transactionService = {
  getAll: async () => {
    return apiRequest('/transactions');
  },

  getById: async (id) => {
    return apiRequest(`/transactions/${id}`);
  },

  create: async (transactionData) => {
    return apiRequest('/transactions', {
      method: 'POST',
      body: transactionData,
    });
  },

  update: async (id, transactionData) => {
    return apiRequest(`/transactions/${id}`, {
      method: 'PUT',
      body: transactionData,
    });
  },

  delete: async (id) => {
    return apiRequest(`/transactions/${id}`, {
      method: 'DELETE',
    });
  },

  search: async (params = {}) => {
    const query = new URLSearchParams();
    if (params.query) query.append('query', params.query);
    if (params.type && params.type !== 'ALL') query.append('type', params.type);
    if (params.category && params.category !== 'All') query.append('category', params.category);
    if (params.startDate) query.append('startDate', params.startDate);
    if (params.endDate) query.append('endDate', params.endDate);
    if (params.minAmount) query.append('minAmount', params.minAmount);
    if (params.maxAmount) query.append('maxAmount', params.maxAmount);

    return apiRequest(`/search?${query.toString()}`);
  },
};
