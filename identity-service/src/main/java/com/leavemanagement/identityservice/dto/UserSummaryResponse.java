package com.leavemanagement.identityservice.dto;

import java.util.Set;

public class UserSummaryResponse {
    private String employeeCode;
    private String name;
    private String email;
    private Set<String> roles;

    public UserSummaryResponse() {
    }

    public UserSummaryResponse(String employeeCode, String name, String email, Set<String> roles) {
        this.employeeCode = employeeCode;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
