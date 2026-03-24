package com.leavemanagement.timesheetservice.service;

import com.leavemanagement.timesheetservice.dto.TimesheetDto;
import java.time.LocalDate;
import java.util.List;

public interface TimesheetService {
    TimesheetDto getTimesheet(String employeeCode, LocalDate weekStartDate);
    TimesheetDto saveOrUpdateTimesheet(TimesheetDto timesheetDto);
    TimesheetDto submitTimesheet(String employeeCode, LocalDate weekStartDate);
    List<TimesheetDto> getPendingApprovals();
    TimesheetDto updateStatus(Long timesheetId, String status, String managerComments);
}
