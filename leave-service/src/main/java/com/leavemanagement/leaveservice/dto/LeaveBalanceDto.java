package com.leavemanagement.leaveservice.dto;

public class LeaveBalanceDto {
    private String leaveType;
    private Double allocated;
    private Double consumed;
    private Double availableBalance;

    public LeaveBalanceDto() {
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
        return availableBalance;
    }

    public void setAvailableBalance(Double availableBalance) {
        this.availableBalance = availableBalance;
    }
}
