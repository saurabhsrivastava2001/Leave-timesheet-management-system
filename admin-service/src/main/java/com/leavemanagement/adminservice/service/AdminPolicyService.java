package com.leavemanagement.adminservice.service;

import com.leavemanagement.adminservice.dto.LeavePolicyDto;
import com.leavemanagement.adminservice.entity.LeavePolicy;
import com.leavemanagement.adminservice.exception.BadRequestException;
import com.leavemanagement.adminservice.exception.ResourceNotFoundException;
import com.leavemanagement.adminservice.repository.LeavePolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminPolicyService {

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    public List<LeavePolicyDto> getAllPolicies() {
        return leavePolicyRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }
createOrUpdatePolicy
    public LeavePolicyDto getPolicyByCode(String code) {
        LeavePolicy policy = leavePolicyRepository.findByPolicyCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with code: " + code));
        return mapToDto(policy);
    }

    public LeavePolicyDto (LeavePolicyDto dto) {
        LeavePolicy policy = leavePolicyRepository.findByPolicyCode(dto.getPolicyCode()).orElse(new LeavePolicy());
        policy.setPolicyCode(dto.getPolicyCode());
        policy.setLeaveType(dto.getLeaveType());
        policy.setAnnualAllocation(dto.getAnnualAllocation());
        policy.setCarryForwardAllowed(dto.isCarryForwardAllowed());
        policy.setMaxCarryForwardDays(dto.getMaxCarryForwardDays());
        
        return mapToDto(leavePolicyRepository.save(policy));
    }

    public void deletePolicy(String code) {
        LeavePolicy policy = leavePolicyRepository.findByPolicyCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with code: " + code));
        leavePolicyRepository.delete(policy);
    }

    private LeavePolicyDto mapToDto(LeavePolicy policy) {
        LeavePolicyDto dto = new LeavePolicyDto();
        dto.setId(policy.getId());
        dto.setPolicyCode(policy.getPolicyCode());
        dto.setLeaveType(policy.getLeaveType());
        dto.setAnnualAllocation(policy.getAnnualAllocation());
        dto.setCarryForwardAllowed(policy.isCarryForwardAllowed());
        dto.setMaxCarryForwardDays(policy.getMaxCarryForwardDays());
        return dto;
    }
}
