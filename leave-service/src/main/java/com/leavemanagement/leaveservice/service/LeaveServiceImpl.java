package com.leavemanagement.leaveservice.service;

import com.leavemanagement.leaveservice.dto.LeaveBalanceDto;
import com.leavemanagement.leaveservice.dto.LeaveRequestDto;
import com.leavemanagement.leaveservice.entity.LeaveBalance;
import com.leavemanagement.leaveservice.entity.LeaveRequest;
import com.leavemanagement.leaveservice.exception.BadRequestException;
import com.leavemanagement.leaveservice.exception.ResourceNotFoundException;
import com.leavemanagement.leaveservice.repository.LeaveBalanceRepository;
import com.leavemanagement.leaveservice.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Override
    public List<LeaveBalanceDto> getLeaveBalances(String employeeCode) {
        return leaveBalanceRepository.findByEmployeeCode(employeeCode).stream().map(this::mapToBalanceDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveRequestDto applyForLeave(String employeeCode, LeaveRequestDto dto) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        // Check overlaps
        List<LeaveRequest> overlaps = leaveRequestRepository.findOverlappingRequests(employeeCode, dto.getStartDate(), dto.getEndDate());
        if (!overlaps.isEmpty()) {
            throw new BadRequestException("Date range overlaps with existing leave");
        }

        long daysApplied = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        // Check balance
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeCodeAndLeaveType(employeeCode, dto.getLeaveType())
                .orElseThrow(() -> new BadRequestException("No leave balance record found for type: " + dto.getLeaveType()));

        if (balance.getAvailableBalance() < daysApplied) {
            throw new BadRequestException("Insufficient balance for leave type: " + dto.getLeaveType());
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployeeCode(employeeCode);
        request.setLeaveType(dto.getLeaveType());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setReason(dto.getReason());
        request.setStatus("SUBMITTED");

        return mapToRequestDto(leaveRequestRepository.save(request));
    }

    @Override
    public List<LeaveRequestDto> getLeaveHistory(String employeeCode) {
        return leaveRequestRepository.findByEmployeeCode(employeeCode).stream().map(this::mapToRequestDto).collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto> getTeamCalendar(LocalDate startDate, LocalDate endDate) {
        // Simple implementation: fetches all approved leaves in range
        return leaveRequestRepository.findAll().stream()
                .filter(lr -> "APPROVED".equals(lr.getStatus()) &&
                        !lr.getStartDate().isAfter(endDate) &&
                        !lr.getEndDate().isBefore(startDate))
                .map(this::mapToRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto> getPendingApprovals() {
        return leaveRequestRepository.findAll().stream()
                .filter(lr -> "SUBMITTED".equals(lr.getStatus()))
                .map(this::mapToRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveRequestDto updateLeaveStatus(Long leaveRequestId, String status, String managerComments) {
        LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave Request not found"));

        if (!"SUBMITTED".equals(request.getStatus())) {
            throw new BadRequestException("Only SUBMITTED requests can be processed");
        }

        if ("REJECTED".equals(status) && (managerComments == null || managerComments.isEmpty())) {
            throw new BadRequestException("Manager comments are required for rejection");
        }

        if ("APPROVED".equals(status)) {
            // Deduct balance
            long daysApplied = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeCodeAndLeaveType(request.getEmployeeCode(), request.getLeaveType())
                    .orElseThrow(() -> new ResourceNotFoundException("Balance not found"));
            
            balance.setConsumed(balance.getConsumed() + daysApplied);
            leaveBalanceRepository.save(balance);
        }

        request.setStatus(status);
        request.setManagerComments(managerComments);
        return mapToRequestDto(leaveRequestRepository.save(request));
    }

    private LeaveBalanceDto mapToBalanceDto(LeaveBalance balance) {
        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setLeaveType(balance.getLeaveType());
        dto.setAllocated(balance.getAllocated());
        dto.setConsumed(balance.getConsumed());
        dto.setAvailableBalance(balance.getAvailableBalance());
        return dto;
    }

    private LeaveRequestDto mapToRequestDto(LeaveRequest request) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(request.getId());
        dto.setLeaveType(request.getLeaveType());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        dto.setManagerComments(request.getManagerComments());
        return dto;
    }
}
