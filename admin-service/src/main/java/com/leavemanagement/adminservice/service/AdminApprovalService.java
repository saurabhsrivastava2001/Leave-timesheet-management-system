package com.leavemanagement.adminservice.service;

import com.leavemanagement.adminservice.client.LeaveClient;
import com.leavemanagement.adminservice.client.TimesheetClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminApprovalService {

    @Autowired
    private TimesheetClient timesheetClient;

    @Autowired
    private LeaveClient leaveClient;

    public List<Map<String, Object>> getPendingTimesheets() {
        return timesheetClient.getPendingTimesheets().getBody();
    }

    public Map<String, Object> approveTimesheet(Long id, String comments) {
        return timesheetClient.updateTimesheetStatus(id, "APPROVED", comments).getBody();
    }

    public Map<String, Object> rejectTimesheet(Long id, String comments) {
        return timesheetClient.updateTimesheetStatus(id, "REJECTED", comments).getBody();
    }

    public List<Map<String, Object>> getPendingLeaves() {
        return leaveClient.getPendingLeaveRequests().getBody();
    }

    public Map<String, Object> approveLeave(Long id, String comments) {
        return leaveClient.updateLeaveStatus(id, "APPROVED", comments).getBody();
    }

    public Map<String, Object> rejectLeave(Long id, String comments) {
        return leaveClient.updateLeaveStatus(id, "REJECTED", comments).getBody();
    }
}
