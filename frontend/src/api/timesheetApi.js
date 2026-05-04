import api from './axios';

export const timesheetApi = {
  // Log weekly timesheet entries
  logEntries: async (timesheetData) => {
    // Via Gateway: /timesheet/api/timesheet/entries
    // timesheetData: { weekStartDate, entries: [{ projectCode, workDate, hours, taskSummary }] }
    // Add employeeCode manually if missing:
    const employeeCode = localStorage.getItem('employeeCode');
    const payload = { ...timesheetData, employeeCode };
    const response = await api.post('/timesheet/api/timesheet/entries', payload);
    return response.data;
  },

  // Submit timesheet
  submitTimesheet: async (weekStartDate) => {
    const response = await api.post(`/timesheet/api/timesheet/weeks/${weekStartDate}/submit`);
    return response.data;
  },

  // (If there was a GET endpoint to fetch them, we'd add it here. The documentation doesn't specify one, but usually it exists)
};
