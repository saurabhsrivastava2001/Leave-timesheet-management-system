package com.leavemanagement.timesheetservice.dto;

public class ProjectSummaryDto {
    private String projectCode;
    private String name;

    public ProjectSummaryDto() {
    }

    public ProjectSummaryDto(String projectCode, String name) {
        this.projectCode = projectCode;
        this.name = name;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
