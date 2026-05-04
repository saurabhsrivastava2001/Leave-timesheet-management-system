package com.leavemanagement.timesheetservice.service;

import com.leavemanagement.timesheetservice.dto.ProjectSummaryDto;
import com.leavemanagement.timesheetservice.dto.TimesheetDto;
import java.time.LocalDate;
import java.util.List;

public interface TimesheetService {
    TimesheetDto getTimesheet(String employeeCode, LocalDate weekStartDate);
    List<TimesheetDto> getTimesheetHistory(String employeeCode);
    List<ProjectSummaryDto> getActiveProjects();
    TimesheetDto saveOrUpdateTimesheet(String employeeCode, TimesheetDto timesheetDto);
    TimesheetDto submitTimesheet(String employeeCode, LocalDate weekStartDate);
    List<TimesheetDto> getPendingApprovals();
    TimesheetDto updateStatus(Long timesheetId, String status, String managerComments);
}
