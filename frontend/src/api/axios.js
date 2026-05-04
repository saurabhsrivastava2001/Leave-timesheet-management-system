import axios from 'axios';

const api = axios.create({
  // No fixed baseURL since each service has a different Gateway prefix
  // e.g., /auth/..., /timesheet/..., etc.
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token');
    const employeeCode = localStorage.getItem('employeeCode');
    const userRoles = localStorage.getItem('userRoles');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    if (employeeCode) {
      config.headers['X-Employee-Code'] = employeeCode;
    }
    if (userRoles) {
      try {
        config.headers['X-User-Roles'] = JSON.parse(userRoles).join(',');
      } catch {
        config.headers['X-User-Roles'] = userRoles;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle global errors (like 401s)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Auto-logout or token refresh logic can go here
      console.error("Unauthorized! Token might be expired.");
      // Optional: Clear token and redirect to login
      // localStorage.removeItem('jwt_token');
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
