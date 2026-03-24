package com.leavemanagement.adminservice.entity;

import jakarta.persistence.*;

@Entity
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String policyCode;

    @Column(nullable = false)
    private String leaveType;

    @Column(nullable = false)
    private Double annualAllocation;

    @Column(nullable = false)
    private boolean carryForwardAllowed;

    private Integer maxCarryForwardDays;

    public LeavePolicy() {
    }

    public LeavePolicy(Long id, String policyCode, String leaveType, Double annualAllocation, boolean carryForwardAllowed, Integer maxCarryForwardDays) {
        this.id = id;
        this.policyCode = policyCode;
        this.leaveType = leaveType;
        this.annualAllocation = annualAllocation;
        this.carryForwardAllowed = carryForwardAllowed;
        this.maxCarryForwardDays = maxCarryForwardDays;
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
