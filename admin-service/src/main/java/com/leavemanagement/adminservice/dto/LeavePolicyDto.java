package com.leavemanagement.adminservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LeavePolicyDto {
    private Long id;

    @NotBlank(message = "Policy code is required")
    private String policyCode;

    @NotBlank(message = "Leave type is required")
    private String leaveType;

    @NotNull(message = "Annual allocation is required")
    private Double annualAllocation;

    private boolean carryForwardAllowed;
    private Integer maxCarryForwardDays;

    public LeavePolicyDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public Double getAnnualAllocation() {
        return annualAllocation;
    }

    public void setAnnualAllocation(Double annualAllocation) {
        this.annualAllocation = annualAllocation;
    }

    public boolean isCarryForwardAllowed() {
        return carryForwardAllowed;
    }

    public void setCarryForwardAllowed(boolean carryForwardAllowed) {
        this.carryForwardAllowed = carryForwardAllowed;
    }

    public Integer getMaxCarryForwardDays() {
        return maxCarryForwardDays;
    }

    public void setMaxCarryForwardDays(Integer maxCarryForwardDays) {
        this.maxCarryForwardDays = maxCarryForwardDays;
    }
}
