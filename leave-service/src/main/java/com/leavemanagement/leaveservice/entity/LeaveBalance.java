package com.leavemanagement.leaveservice.entity;

import jakarta.persistence.*;

@Entity
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeCode;

    @Column(nullable = false)
    private String leaveType;

    @Column(nullable = false)
    private Double allocated;

    @Column(nullable = false)
    private Double consumed;

    public LeaveBalance() {
    }

    public LeaveBalance(Long id, String employeeCode, String leaveType, Double allocated, Double consumed) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.leaveType = leaveType;
        this.allocated = allocated;
        this.consumed = consumed;
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

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public Double getAllocated() {
        return allocated;
    }

    public void setAllocated(Double allocated) {
        this.allocated = allocated;
    }

    public Double getConsumed() {
        return consumed;
    }

    public void setConsumed(Double consumed) {
        this.consumed = consumed;
    }

    public Double getAvailableBalance() {
        return allocated - consumed;
    }
}
