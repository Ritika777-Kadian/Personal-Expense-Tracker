import { apiRequest } from './api';

export const settingsService = {
  getSettings: async () => {
    return apiRequest('/settings');
  },

  updateSettings: async (settingsData) => {
    return apiRequest('/settings', {
      method: 'PUT',
      body: settingsData,
    });
  },
};
