package com.leavemanagement.adminservice.controller;

import com.leavemanagement.adminservice.service.AdminApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/approvals")
@Tag(name = "Admin Approvals", description = "Centralized Approval Endpoints for Managers and Admins")
public class AdminApprovalController {

    @Autowired
    private AdminApprovalService adminApprovalService;

    @GetMapping("/timesheets")
    @Operation(summary = "Get pending timesheets")
    public ResponseEntity<List<Map<String, Object>>> getPendingTimesheets() {
        return ResponseEntity.ok(adminApprovalService.getPendingTimesheets());
    }

    @PostMapping("/timesheets/{id}/approve")
    @Operation(summary = "Approve timesheet")
    public ResponseEntity<Map<String, Object>> approveTimesheet(@PathVariable Long id, @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(adminApprovalService.approveTimesheet(id, comments));
    }

    @PostMapping("/timesheets/{id}/reject")
    @Operation(summary = "Reject timesheet")
    public ResponseEntity<Map<String, Object>> rejectTimesheet(@PathVariable Long id, @RequestParam String comments) {
        return ResponseEntity.ok(adminApprovalService.rejectTimesheet(id, comments));
    }

    @GetMapping("/leaves")
    @Operation(summary = "Get pending leave requests")
    public ResponseEntity<List<Map<String, Object>>> getPendingLeaves() {
        return ResponseEntity.ok(adminApprovalService.getPendingLeaves());
    }

    @PostMapping("/leaves/{id}/approve")
    @Operation(summary = "Approve leave request")
    public ResponseEntity<Map<String, Object>> approveLeave(@PathVariable Long id, @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(adminApprovalService.approveLeave(id, comments));
    }

    @PostMapping("/leaves/{id}/reject")
    @Operation(summary = "Reject leave request")
    public ResponseEntity<Map<String, Object>> rejectLeave(@PathVariable Long id, @RequestParam String comments) {
        return ResponseEntity.ok(adminApprovalService.rejectLeave(id, comments));
    }
}
