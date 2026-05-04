import api from './axios';

export const adminApi = {
  // Get pending timesheets
  getPendingTimesheets: async () => {
    const response = await api.get('/admin/api/admin/approvals/timesheets');
    return response.data;
  },

  // Get pending leaves
  getPendingLeaves: async () => {
    const response = await api.get('/admin/api/admin/approvals/leaves');
    return response.data;
  },

  // Create Policy
  createPolicy: async (policyData) => {
    const response = await api.post('/admin/api/admin/master/policies', policyData);
    return response.data;
  },

  // Create Holiday (Leave Service handles this, mapped to gateway /leave/**)
  createHoliday: async (holidayData) => {
    const response = await api.post('/leave/api/holidays', holidayData);
    return response.data;
  },

  // Approve Timesheet
  approveTimesheet: async (timesheetId, status, comments) => {
    const endpoint = status === 'APPROVED' ? 'approve' : 'reject';
    const response = await api.post(`/admin/api/admin/approvals/timesheets/${timesheetId}/${endpoint}?comments=${encodeURIComponent(comments)}`);
    return response.data;
  },

  // Approve Leave
  approveLeave: async (leaveId, status, comments) => {
    const endpoint = status === 'APPROVED' ? 'approve' : 'reject';
    const response = await api.post(`/admin/api/admin/approvals/leaves/${leaveId}/${endpoint}?comments=${encodeURIComponent(comments)}`);
    return response.data;
  }
};
