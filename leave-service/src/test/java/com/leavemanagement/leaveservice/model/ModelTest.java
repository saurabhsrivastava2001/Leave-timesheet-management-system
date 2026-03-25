package com.leavemanagement.leaveservice.model;

import com.leavemanagement.leaveservice.dto.HolidayDto;
import com.leavemanagement.leaveservice.dto.LeaveBalanceDto;
import com.leavemanagement.leaveservice.dto.LeaveRequestDto;
import com.leavemanagement.leaveservice.entity.Holiday;
import com.leavemanagement.leaveservice.entity.LeaveBalance;
import com.leavemanagement.leaveservice.entity.LeaveRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelTest {

    @Test
    void testDtoCoverage() {
        HolidayDto hd = new HolidayDto();
        hd.setDate(LocalDate.now());
        hd.setDescription("Test");
        assertEquals("Test", hd.getDescription());
        assertEquals(LocalDate.now(), hd.getDate());

        LeaveBalanceDto ld = new LeaveBalanceDto();
        ld.setLeaveType("SICK");
        ld.setAllocated(10.0);
        ld.setConsumed(2.0);
        ld.setAvailableBalance(8.0);
        assertEquals(10.0, ld.getAllocated());
        assertEquals(2.0, ld.getConsumed());
        assertEquals(8.0, ld.getAvailableBalance());
        assertEquals("SICK", ld.getLeaveType());

        LeaveRequestDto lr = new LeaveRequestDto();
        lr.setId(1L);
        lr.setLeaveType("ANNUAL");
        lr.setStartDate(LocalDate.now());
        lr.setEndDate(LocalDate.now());
        lr.setStatus("APPROVED");
        lr.setReason("Vacation");
        lr.setManagerComments("OK");
        assertEquals("ANNUAL", lr.getLeaveType());
        assertEquals(1L, lr.getId());
        assertEquals("Vacation", lr.getReason());
        assertEquals("APPROVED", lr.getStatus());
        assertEquals("OK", lr.getManagerComments());
        assertEquals(LocalDate.now(), lr.getStartDate());
        assertEquals(LocalDate.now(), lr.getEndDate());
    }

    @Test
    void testEntityCoverage() {
        Holiday h = new Holiday();
        h.setId(1L);
        h.setDate(LocalDate.now());
        h.setDescription("Holiday");
        assertEquals(1L, h.getId());
        assertEquals("Holiday", h.getDescription());
        assertEquals(LocalDate.now(), h.getDate());

        LeaveBalance lb = new LeaveBalance();
        lb.setId(1L);
        lb.setEmployeeCode("E1");
        lb.setLeaveType("SICK");
        lb.setAllocated(5.0);
        lb.setConsumed(1.0);
        assertEquals(1L, lb.getId());
        assertEquals("E1", lb.getEmployeeCode());
        assertEquals(4.0, lb.getAvailableBalance());
        assertEquals("SICK", lb.getLeaveType());
        assertEquals(5.0, lb.getAllocated());
        assertEquals(1.0, lb.getConsumed());

        LeaveRequest lr = new LeaveRequest();
        lr.setId(1L);
        lr.setEmployeeCode("E1");
        lr.setLeaveType("SICK");
        lr.setStartDate(LocalDate.now());
        lr.setEndDate(LocalDate.now());
        lr.setReason("Sick");
        lr.setStatus("SUBMITTED");
        lr.setManagerComments("None");
        lr.setCreatedOn(LocalDateTime.now());
        lr.setUpdatedOn(LocalDateTime.now());
        
        // Test JPA Lifecycle Hooks
        lr.prePersist();
        lr.preUpdate();

        assertEquals(1L, lr.getId());
        assertEquals("E1", lr.getEmployeeCode());
        assertEquals("SICK", lr.getLeaveType());
        assertEquals(LocalDate.now(), lr.getStartDate());
        assertEquals(LocalDate.now(), lr.getEndDate());
        assertEquals("Sick", lr.getReason());
        assertEquals("SUBMITTED", lr.getStatus());
        assertEquals("None", lr.getManagerComments());
        assertEquals(true, lr.getCreatedOn() != null);
        assertEquals(true, lr.getUpdatedOn() != null);
        
        LeaveRequest lr2 = new LeaveRequest(2L, "E2", "ANNUAL", LocalDate.now(), LocalDate.now(), "V", "A", "O", LocalDateTime.now(), LocalDateTime.now());
        assertEquals(2L, lr2.getId());
    }
}
