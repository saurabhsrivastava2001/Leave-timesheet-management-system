package com.leavemanagement.adminservice.model;

import com.leavemanagement.adminservice.dto.LeavePolicyDto;
import com.leavemanagement.adminservice.entity.LeavePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminModelTest {

    @Test
    void testDtoCoverage() {
        LeavePolicyDto dto = new LeavePolicyDto();
        dto.setId(1L);
        dto.setPolicyCode("SICK");
        dto.setLeaveType("Sick Leave");
        dto.setAnnualAllocation(10.0);
        dto.setCarryForwardAllowed(true);
        dto.setMaxCarryForwardDays(5);
        
        assertEquals(1L, dto.getId());
        assertEquals("SICK", dto.getPolicyCode());
        assertEquals("Sick Leave", dto.getLeaveType());
        assertEquals(10.0, dto.getAnnualAllocation());
        assertEquals(true, dto.isCarryForwardAllowed());
        assertEquals(5, dto.getMaxCarryForwardDays());
    }

    @Test
    void testEntityCoverage() {
        LeavePolicy entity = new LeavePolicy();
        entity.setId(1L);
        entity.setPolicyCode("SICK");
        entity.setLeaveType("Sick Leave");
        entity.setAnnualAllocation(10.0);
        entity.setCarryForwardAllowed(true);
        entity.setMaxCarryForwardDays(5);
        
        assertEquals(1L, entity.getId());
        assertEquals("SICK", entity.getPolicyCode());
        assertEquals("Sick Leave", entity.getLeaveType());
        assertEquals(10.0, entity.getAnnualAllocation());
        assertEquals(true, entity.isCarryForwardAllowed());
        assertEquals(5, entity.getMaxCarryForwardDays());
        
        LeavePolicy e2 = new LeavePolicy(2L, "ANNUAL", "Annual", 20.0, false, 0);
        assertEquals(2L, e2.getId());
    }
}
