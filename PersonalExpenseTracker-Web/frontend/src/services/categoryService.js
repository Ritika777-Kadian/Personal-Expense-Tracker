import { apiRequest } from './api';

export const categoryService = {
  getAll: async (type) => {
    const url = type ? `/categories?type=${type}` : '/categories';
    return apiRequest(url);
  },

  create: async (categoryData) => {
    return apiRequest('/categories', {
      method: 'POST',
      body: categoryData,
    });
  },

  update: async (id, categoryData) => {
    return apiRequest(`/categories/${id}`, {
      method: 'PUT',
      body: categoryData,
    });
  },

  delete: async (id) => {
    return apiRequest(`/categories/${id}`, {
      method: 'DELETE',
    });
  },
};
