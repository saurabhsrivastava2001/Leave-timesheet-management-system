import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if token exists on load
    const token = localStorage.getItem('jwt_token');
    const storedRoles = localStorage.getItem('userRoles');
    const storedCode = localStorage.getItem('employeeCode');

    if (token) {
      setIsAuthenticated(true);
      setUser({ 
        employeeCode: storedCode || 'Unknown',
        roles: storedRoles ? JSON.parse(storedRoles) : ['ROLE_EMPLOYEE']
      });
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    try {
      const usernameOrEmail = username.trim();
      const response = await api.post('/auth/api/auth/login', { usernameOrEmail, password });
      
      const token = response.data.token || response.data;
      const roles = response.data.roles || ['ROLE_EMPLOYEE'];

      if (token && typeof token === 'string') {
        localStorage.setItem('jwt_token', token);
        localStorage.setItem('employeeCode', response.data.employeeCode || usernameOrEmail);
        localStorage.setItem('userRoles', JSON.stringify(roles));
        
        setIsAuthenticated(true);
        setUser({ 
          employeeCode: response.data.employeeCode || usernameOrEmail,
          roles: roles
        });
        return true;
      }
      return false;
    } catch (error) {
      console.error("Login failed", error);
      throw error;
    }
  };

  const signup = async (employeeCode, name, email, password) => {
    try {
      const payload = { employeeCode, name, email, password };
      await api.post('/auth/api/auth/signup', payload);
      return true; // Success
    } catch (error) {
      console.error("Signup failed", error);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('employeeCode');
    localStorage.removeItem('userRoles');
    setIsAuthenticated(false);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, login, signup, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
