import { apiRequest, setAuthToken } from './api';

export const authService = {
  login: async (username, password) => {
    const data = await apiRequest('/auth/login', {
      method: 'POST',
      body: { username, password },
    });
    if (data?.token) {
      setAuthToken(data.token);
    }
    return data;
  },

  register: async (username, email, password, fullName) => {
    const data = await apiRequest('/auth/register', {
      method: 'POST',
      body: { username, email, password, fullName },
    });
    if (data?.token) {
      setAuthToken(data.token);
    }
    return data;
  },

  getCurrentUser: async () => {
    return apiRequest('/auth/me');
  },

  logout: () => {
    setAuthToken(null);
  },
};
