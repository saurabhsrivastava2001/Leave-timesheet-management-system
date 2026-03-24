package com.leavemanagement.leaveservice.service;

import com.leavemanagement.leaveservice.dto.LeaveBalanceDto;
import com.leavemanagement.leaveservice.dto.LeaveRequestDto;

import java.time.LocalDate;
import java.util.List;

public interface LeaveService {
    List<LeaveBalanceDto> getLeaveBalances(String employeeCode);
    LeaveRequestDto applyForLeave(String employeeCode, LeaveRequestDto leaveRequestDto);
    List<LeaveRequestDto> getLeaveHistory(String employeeCode);
    List<LeaveRequestDto> getTeamCalendar(LocalDate startDate, LocalDate endDate);
    List<LeaveRequestDto> getPendingApprovals();
    LeaveRequestDto updateLeaveStatus(Long leaveRequestId, String status, String managerComments);
}
