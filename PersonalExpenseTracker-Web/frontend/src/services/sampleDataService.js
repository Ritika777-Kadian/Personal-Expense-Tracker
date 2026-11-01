import { apiRequest } from './api';

export const sampleDataService = {
  getStatus: async () => {
    return apiRequest('/sample-data/status');
  },

  seed: async (year, month) => {
    const query = new URLSearchParams();
    if (year) query.append('year', year);
    if (month) query.append('month', month);
    return apiRequest(`/sample-data/seed?${query.toString()}`, {
      method: 'POST',
    });
  },

  purge: async () => {
    return apiRequest('/sample-data/purge', {
      method: 'POST',
    });
  },
};
