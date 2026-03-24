package com.leavemanagement.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "leave-service", path = "/api/leave")
public interface LeaveClient {

    @GetMapping("/pending-approvals")
    ResponseEntity<List<Map<String, Object>>> getPendingLeaveRequests();

    @PutMapping("/{id}/status")
    ResponseEntity<Map<String, Object>> updateLeaveStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "comments", required = false) String comments);
}
