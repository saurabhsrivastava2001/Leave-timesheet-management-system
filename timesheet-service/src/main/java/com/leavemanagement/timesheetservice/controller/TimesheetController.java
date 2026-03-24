package com.leavemanagement.timesheetservice.controller;

import com.leavemanagement.timesheetservice.dto.TimesheetDto;

// touch: refresh IDE index

import com.leavemanagement.timesheetservice.service.TimesheetService;
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
@RequestMapping("/api/timesheet")
@Tag(name = "Timesheet", description = "Timesheet Management APIs")
public class TimesheetController {

    @Autowired
    private TimesheetService timesheetService;

    @GetMapping("/weeks/{weekStart}")
    @Operation(summary = "Get timesheet for the week")
    public ResponseEntity<TimesheetDto> getTimesheet(
            @RequestHeader("X-Employee-Code") String employeeCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(timesheetService.getTimesheet(employeeCode, weekStart));
    }

    @PostMapping("/entries")
    @Operation(summary = "Save or update timesheet draft")
    public ResponseEntity<TimesheetDto> saveTimesheet(
            @RequestHeader("X-Employee-Code") String employeeCode,
            @Valid @RequestBody TimesheetDto timesheetDto) {
        timesheetDto.setEmployeeCode(employeeCode);
        return ResponseEntity.ok(timesheetService.saveOrUpdateTimesheet(timesheetDto));
    }

    @PostMapping("/weeks/{weekStart}/submit")
    @Operation(summary = "Submit a timesheet for manager review")
    public ResponseEntity<TimesheetDto> submitTimesheet(
            @RequestHeader("X-Employee-Code") String employeeCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(timesheetService.submitTimesheet(employeeCode, weekStart));
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get all submitted timesheets pending approval")
    public ResponseEntity<List<TimesheetDto>> getPendingApprovals() {
        return ResponseEntity.ok(timesheetService.getPendingApprovals());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Approve or reject a timesheet")
    public ResponseEntity<TimesheetDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(timesheetService.updateStatus(id, status, comments));
    }
}