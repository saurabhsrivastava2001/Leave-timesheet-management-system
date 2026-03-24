package com.leavemanagement.leaveservice.controller;

import com.leavemanagement.leaveservice.dto.LeaveBalanceDto;
import com.leavemanagement.leaveservice.dto.LeaveRequestDto;
import com.leavemanagement.leaveservice.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave")
@Tag(name = "Leave Management", description = "Endpoints for Leave Requests and Balances")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @GetMapping("/balance/{userId}")
    @Operation(summary = "Get leave balances by userId/employeeCode")
    public ResponseEntity<List<LeaveBalanceDto>> getLeaveBalances(@PathVariable String userId) {
        return ResponseEntity.ok(leaveService.getLeaveBalances(userId));
    }

    @PostMapping("/requests")
    @Operation(summary = "Create a new leave request")
    public ResponseEntity<LeaveRequestDto> createLeaveRequest(
            @RequestHeader("X-Employee-Code") String employeeCode,
            @Valid @RequestBody LeaveRequestDto requestDto) {
        return ResponseEntity.ok(leaveService.applyForLeave(employeeCode, requestDto));
    }

    @GetMapping("/history")
    @Operation(summary = "Get leave history for employee")
    public ResponseEntity<List<LeaveRequestDto>> getLeaveHistory(
            @RequestHeader("X-Employee-Code") String employeeCode) {
        return ResponseEntity.ok(leaveService.getLeaveHistory(employeeCode));
    }

    @GetMapping("/team-calendar")
    @Operation(summary = "Return team leave calendar")
    public ResponseEntity<List<LeaveRequestDto>> getTeamCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(leaveService.getTeamCalendar(startDate, endDate));
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Return pending leave requests for manager approval")
    public ResponseEntity<List<LeaveRequestDto>> getPendingApprovals() {
        return ResponseEntity.ok(leaveService.getPendingApprovals());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Approve or reject a leave request")
    public ResponseEntity<LeaveRequestDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(leaveService.updateLeaveStatus(id, status, comments));
    }
}
