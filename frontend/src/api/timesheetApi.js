import api from './axios';

export const timesheetApi = {
  getWeek: async (weekStartDate) => {
    try {
      const response = await api.get(`/timesheet/api/timesheet/weeks/${weekStartDate}`);
      return response.data;
    } catch (error) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  getHistory: async () => {
    const response = await api.get('/timesheet/api/timesheet/history');
    return response.data;
  },

  getProjects: async () => {
    const response = await api.get('/timesheet/api/timesheet/projects');
    return response.data;
  },

  saveWeek: async (weekStartDate, timesheetData) => {
    const response = await api.put(`/timesheet/api/timesheet/weeks/${weekStartDate}`, timesheetData);
    return response.data;
  },

  submitTimesheet: async (weekStartDate) => {
    const response = await api.post(`/timesheet/api/timesheet/weeks/${weekStartDate}/submit`);
    return response.data;
  },
};
