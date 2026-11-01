const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

export const getAuthToken = () => {
  return localStorage.getItem('pet_token');
};

export const setAuthToken = (token) => {
  if (token) {
    localStorage.setItem('pet_token', token);
  } else {
    localStorage.removeItem('pet_token');
  }
};

export const apiRequest = async (endpoint, options = {}) => {
  const token = getAuthToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {}),
  };

  // If body is FormData or already a string
  const isJson = options.body && !(options.body instanceof FormData) && typeof options.body === 'object';
  const body = isJson ? JSON.stringify(options.body) : options.body;

  const config = {
    ...options,
    headers,
    body,
  };

  const url = `${API_BASE_URL}${endpoint}`;
  const response = await fetch(url, config);

  if (response.status === 401) {
    // Unauthorized: clear token and redirect to login
    setAuthToken(null);
    if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
      window.location.href = '/login';
    }
  }

  // Handle file downloads (blob)
  const contentType = response.headers.get('content-type');
  if (contentType && (contentType.includes('text/csv') || contentType.includes('application/octet-stream'))) {
    if (!response.ok) {
      throw new Error('Download failed');
    }
    return response.blob();
  }

  // 204 No Content
  if (response.status === 204) {
    return null;
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || data?.error || `Request failed with status ${response.status}`;
    throw new Error(errorMsg);
  }

  return data;
};
