import api from './axios';

export const leaveApi = {
  // Get leave balance
  getBalance: async (employeeCode) => {
    const response = await api.get(`/leave/api/leave/balance/${employeeCode}`);
    return response.data;
  },

  // Get employee's leaves
  getMyLeaves: async (employeeCode) => {
    // Controller mapping is /history, requires X-Employee-Code but our axios handles it implicitly now via local token
    const response = await api.get(`/leave/api/leave/history`);
    return response.data;
  },

  // Apply for leave
  applyLeave: async (leaveData) => {
    const response = await api.post('/leave/api/leave/requests', leaveData);
    return response.data;
  }
};
