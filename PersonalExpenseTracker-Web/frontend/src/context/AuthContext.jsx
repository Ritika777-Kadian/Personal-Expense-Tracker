import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';
import { settingsService } from '../services/settingsService';
import { sampleDataService } from '../services/sampleDataService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [settings, setSettings] = useState({
    currency: 'INR',
    currencySymbol: '\u20B9',
    dateFormat: 'YYYY-MM-DD',
    theme: 'light',
    firstLaunchPromptShown: false,
  });
  const [demoDataActive, setDemoDataActive] = useState(false);
  const [loading, setLoading] = useState(true);

  const initAuth = async () => {
    try {
      const token = localStorage.getItem('pet_token');
      if (token) {
        const userData = await authService.getCurrentUser();
        setUser(userData);
        const userSettings = await settingsService.getSettings();
        setSettings(userSettings);
        const demoStatus = await sampleDataService.getStatus();
        setDemoDataActive(demoStatus?.hasSampleData || false);
      }
    } catch (err) {
      console.error('Session initialization error:', err);
      authService.logout();
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    initAuth();
  }, []);

  const login = async (username, password) => {
    const data = await authService.login(username, password);
    setUser({
      id: data.id,
      username: data.username,
      email: data.email,
      fullName: data.fullName,
    });
    setSettings((prev) => ({
      ...prev,
      currency: data.currency || 'INR',
      currencySymbol: data.currencySymbol || '\u20B9',
    }));
    const demoStatus = await sampleDataService.getStatus().catch(() => ({ hasSampleData: false }));
    setDemoDataActive(demoStatus?.hasSampleData || false);
    return data;
  };

  const register = async (username, email, password, fullName) => {
    const data = await authService.register(username, email, password, fullName);
    setUser({
      id: data.id,
      username: data.username,
      email: data.email,
      fullName: data.fullName,
    });
    setSettings((prev) => ({
      ...prev,
      currency: data.currency || 'INR',
      currencySymbol: data.currencySymbol || '\u20B9',
    }));
    return data;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setDemoDataActive(false);
  };

  const refreshSettings = async () => {
    try {
      const updated = await settingsService.getSettings();
      setSettings(updated);
      return updated;
    } catch (err) {
      console.error('Failed to refresh settings', err);
    }
  };

  const updateSettings = async (newSettings) => {
    const updated = await settingsService.updateSettings(newSettings);
    setSettings(updated);
    return updated;
  };

  const refreshDemoStatus = async () => {
    try {
      const status = await sampleDataService.getStatus();
      setDemoDataActive(status?.hasSampleData || false);
      return status;
    } catch (err) {
      console.error('Failed to check demo data status', err);
      return null;
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        settings,
        currencySymbol: settings.currencySymbol || '\u20B9',
        isAuthenticated: !!user,
        loading,
        login,
        register,
        logout,
        refreshSettings,
        updateSettings,
        demoDataActive,
        refreshDemoStatus,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
