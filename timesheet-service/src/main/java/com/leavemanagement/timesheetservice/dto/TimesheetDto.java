package com.leavemanagement.timesheetservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class TimesheetDto {
    private Long id;
    
    
    private String employeeCode;
    
    @NotNull(message = "Week start date is required")
    private LocalDate weekStartDate;
    
    private String status;
    private String managerComments;
    
    @Valid
    private List<TimesheetEntryDto> entries;

    public TimesheetDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getManagerComments() {
        return managerComments;
    }

    public void setManagerComments(String managerComments) {
        this.managerComments = managerComments;
    }

    public List<TimesheetEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<TimesheetEntryDto> entries) {
        this.entries = entries;
    }
}
