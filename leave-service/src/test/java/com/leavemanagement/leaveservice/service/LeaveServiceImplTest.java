package com.leavemanagement.leaveservice.service;

import com.leavemanagement.leaveservice.dto.LeaveBalanceDto;
import com.leavemanagement.leaveservice.dto.LeaveRequestDto;
import com.leavemanagement.leaveservice.entity.LeaveBalance;
import com.leavemanagement.leaveservice.entity.LeaveRequest;
import com.leavemanagement.leaveservice.exception.BadRequestException;
import com.leavemanagement.leaveservice.exception.ResourceNotFoundException;
import com.leavemanagement.leaveservice.repository.LeaveBalanceRepository;
import com.leavemanagement.leaveservice.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private LeaveBalance balance;
    private LeaveRequestDto requestDto;

    @BeforeEach
    void setUp() {
        balance = new LeaveBalance();
        balance.setEmployeeCode("EMP001");
        balance.setLeaveType("SICK");
        balance.setAllocated(10.0);
        balance.setConsumed(0.0);

        requestDto = new LeaveRequestDto();
        requestDto.setLeaveType("SICK");
        requestDto.setStartDate(LocalDate.now().plusDays(1));
        requestDto.setEndDate(LocalDate.now().plusDays(2));
        requestDto.setReason("Feeling unwell");
    }

    @Test
    void testGetLeaveBalances_Success() {
        when(leaveBalanceRepository.findByEmployeeCode("EMP001")).thenReturn(Collections.singletonList(balance));
        List<LeaveBalanceDto> balances = leaveService.getLeaveBalances("EMP001");
        assertEquals(1, balances.size());
        assertEquals("SICK", balances.get(0).getLeaveType());
    }

    @Test
    void testApplyForLeave_Success() {
        when(leaveRequestRepository.findOverlappingRequests(eq("EMP001"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(leaveBalanceRepository.findByEmployeeCodeAndLeaveType("EMP001", "SICK"))
                .thenReturn(Optional.of(balance));

        LeaveRequest savedRequest = new LeaveRequest();
        savedRequest.setId(1L);
        savedRequest.setLeaveType("SICK");
        savedRequest.setStatus("SUBMITTED");
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(savedRequest);

        LeaveRequestDto result = leaveService.applyForLeave("EMP001", requestDto);

        assertNotNull(result);
        assertEquals("SUBMITTED", result.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void testApplyForLeave_StartDateAfterEndDate() {
        requestDto.setStartDate(LocalDate.now().plusDays(5));
        requestDto.setEndDate(LocalDate.now().plusDays(1));

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> leaveService.applyForLeave("EMP001", requestDto));
        assertEquals("Start date cannot be after end date", exception.getMessage());
    }

    @Test
    void testApplyForLeave_OverlappingLeave() {
        when(leaveRequestRepository.findOverlappingRequests(eq("EMP001"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(new LeaveRequest()));

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> leaveService.applyForLeave("EMP001", requestDto));
        assertEquals("Date range overlaps with existing leave", exception.getMessage());
    }

    @Test
    void testApplyForLeave_InsufficientBalance() {
        balance.setAllocated(0.0);
        when(leaveRequestRepository.findOverlappingRequests(eq("EMP001"), any(), any())).thenReturn(Collections.emptyList());
        when(leaveBalanceRepository.findByEmployeeCodeAndLeaveType("EMP001", "SICK")).thenReturn(Optional.of(balance));

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> leaveService.applyForLeave("EMP001", requestDto));
        assertEquals("Insufficient balance for leave type: SICK", exception.getMessage());
    }

    @Test
    void testUpdateLeaveStatus_Approve_Success() {
        LeaveRequest request = new LeaveRequest();
        request.setId(1L);
        request.setEmployeeCode("EMP001");
        request.setLeaveType("SICK");
        request.setStatus("SUBMITTED");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1)); // 2 days

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(leaveBalanceRepository.findByEmployeeCodeAndLeaveType("EMP001", "SICK")).thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(request);

        LeaveRequestDto result = leaveService.updateLeaveStatus(1L, "APPROVED", "Approved");

        assertEquals("APPROVED", result.getStatus());
        assertEquals(2.0, balance.getConsumed()); // Validates balance deduction
        verify(leaveBalanceRepository, times(1)).save(balance);
    }

    @Test
    void testUpdateLeaveStatus_RejectWithoutComments() {
        LeaveRequest request = new LeaveRequest();
        request.setId(1L);
        request.setStatus("SUBMITTED");
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> leaveService.updateLeaveStatus(1L, "REJECTED", ""));
        assertEquals("Manager comments are required for rejection", exception.getMessage());
    }

    @Test
    void testUpdateLeaveStatus_NotSubmitted() {
        LeaveRequest request = new LeaveRequest();
        request.setId(1L);
        request.setStatus("APPROVED"); // Already approved
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(BadRequestException.class, () -> leaveService.updateLeaveStatus(1L, "REJECTED", "Sorry"));
    }

    @Test
    void testGetTeamCalendar() {
        LeaveRequest request = new LeaveRequest();
        request.setId(1L);
        request.setStatus("APPROVED");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(2));
        
        when(leaveRequestRepository.findAll()).thenReturn(Collections.singletonList(request));
        
        List<LeaveRequestDto> result = leaveService.getTeamCalendar(LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
        assertEquals(1, result.size());
    }
}
