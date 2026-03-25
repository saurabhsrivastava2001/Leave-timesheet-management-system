package com.leavemanagement.adminservice.service;

import com.leavemanagement.adminservice.dto.LeavePolicyDto;
import com.leavemanagement.adminservice.entity.LeavePolicy;
import com.leavemanagement.adminservice.exception.ResourceNotFoundException;
import com.leavemanagement.adminservice.repository.LeavePolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminPolicyServiceTest {

    @Mock
    private LeavePolicyRepository leavePolicyRepository;

    @InjectMocks
    private AdminPolicyService adminPolicyService;

    @Test
    void testGetAllPolicies() {
        LeavePolicy policy = new LeavePolicy(1L, "ANNUAL", "Annual Leave", 20.0, true, 5);
        when(leavePolicyRepository.findAll()).thenReturn(Collections.singletonList(policy));

        List<LeavePolicyDto> result = adminPolicyService.getAllPolicies();
        assertEquals(1, result.size());
        assertEquals("Annual Leave", result.get(0).getLeaveType());
    }

    @Test
    void testGetPolicyByCode() {
        LeavePolicy policy = new LeavePolicy(1L, "ANNUAL", "Annual Leave", 20.0, true, 5);
        when(leavePolicyRepository.findByPolicyCode("ANNUAL")).thenReturn(Optional.of(policy));

        LeavePolicyDto result = adminPolicyService.getPolicyByCode("ANNUAL");
        assertEquals("Annual Leave", result.getLeaveType());
    }

    @Test
    void testGetPolicyByCode_NotFound() {
        when(leavePolicyRepository.findByPolicyCode("ANNUAL")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminPolicyService.getPolicyByCode("ANNUAL"));
    }

    @Test
    void testCreateOrUpdatePolicy_New() {
        LeavePolicyDto dto = new LeavePolicyDto();
        dto.setPolicyCode("NEW");
        dto.setLeaveType("New Leave");

        when(leavePolicyRepository.findByPolicyCode("NEW")).thenReturn(Optional.empty());
        when(leavePolicyRepository.save(any(LeavePolicy.class))).thenAnswer(i -> {
            LeavePolicy p = i.getArgument(0);
            p.setId(10L);
            return p;
        });

        LeavePolicyDto result = adminPolicyService.createOrUpdatePolicy(dto);
        assertEquals(10L, result.getId());
        assertEquals("NEW", result.getPolicyCode());
    }

    @Test
    void testDeletePolicy() {
        LeavePolicy policy = new LeavePolicy(1L, "ANNUAL", "Annual Leave", 20.0, true, 5);
        when(leavePolicyRepository.findByPolicyCode("ANNUAL")).thenReturn(Optional.of(policy));

        adminPolicyService.deletePolicy("ANNUAL");
        verify(leavePolicyRepository, times(1)).delete(policy);
    }
}
