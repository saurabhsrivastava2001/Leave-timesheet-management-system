package com.leavemanagement.adminservice.controller;

import com.leavemanagement.adminservice.dto.LeavePolicyDto;
import com.leavemanagement.adminservice.service.AdminPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/master/policies")
@Tag(name = "Master Data Policies", description = "Admin configuration for policies")
public class AdminPolicyController {

    @Autowired
    private AdminPolicyService adminPolicyService;

    @GetMapping
    @Operation(summary = "Get all leave policies")
    public ResponseEntity<List<LeavePolicyDto>> getAllPolicies() {
        return ResponseEntity.ok(adminPolicyService.getAllPolicies());
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get policy by code")
    public ResponseEntity<LeavePolicyDto> getPolicyByCode(@PathVariable String code) {
        return ResponseEntity.ok(adminPolicyService.getPolicyByCode(code));
    }

    @PostMapping
    @Operation(summary = "Create or update leave policy")
    public ResponseEntity<LeavePolicyDto> createOrUpdatePolicy(@Valid @RequestBody LeavePolicyDto policyDto) {
        return ResponseEntity.ok(adminPolicyService.createOrUpdatePolicy(policyDto));
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "Delete leave policy")
    public ResponseEntity<Void> deletePolicy(@PathVariable String code) {
        adminPolicyService.deletePolicy(code);
        return ResponseEntity.noContent().build();
    }
}
