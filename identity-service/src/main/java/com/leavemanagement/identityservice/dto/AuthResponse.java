package com.leavemanagement.identityservice.dto;

import java.util.Set;

public class AuthResponse {
    private String token;
    private String employeeCode;
    private String name;
    private Set<String> roles;

    public AuthResponse() {
    }

    public AuthResponse(String token, String employeeCode, String name, Set<String> roles) {
        this.token = token;
        this.employeeCode = employeeCode;
        this.name = name;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
