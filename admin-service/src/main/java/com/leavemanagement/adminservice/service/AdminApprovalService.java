package com.leavemanagement.adminservice.service;

import com.leavemanagement.adminservice.client.IdentityClient;
import com.leavemanagement.adminservice.client.LeaveClient;
import com.leavemanagement.adminservice.client.TimesheetClient;
import com.leavemanagement.adminservice.config.RabbitMQConfig;
import com.leavemanagement.adminservice.exception.BadRequestException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class AdminApprovalService {

    @Autowired
    private TimesheetClient timesheetClient;

    @Autowired
    private LeaveClient leaveClient;

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // =========================================================================
    // SYNCHRONOUS READS (Protected by CircuitBreaker fallback methods)
    // =========================================================================
    
    @CircuitBreaker(name = "timesheetService", fallbackMethod = "fallbackGetPendingTimesheets")
    public List<Map<String, Object>> getPendingTimesheets() {
        return timesheetClient.getPendingTimesheets().getBody();
    }

    public List<Map<String, Object>> fallbackGetPendingTimesheets(Throwable t) {
        return List.of(Map.of("error", "Timesheet Service is temporarily unavailable. Cannot safely fetch pending timesheets at this time."));
    }

    @CircuitBreaker(name = "leaveService", fallbackMethod = "fallbackGetPendingLeaves")
    public List<Map<String, Object>> getPendingLeaves() {
        return leaveClient.getPendingLeaveRequests().getBody();
    }

    public List<Map<String, Object>> fallbackGetPendingLeaves(Throwable t) {
        return List.of(Map.of("error", "Leave Service is temporarily unavailable. Cannot safely fetch pending leaves at this time."));
    }

    // =========================================================================
    // ASYNCHRONOUS WRITES (Event streaming to RabbitMQ)
    // =========================================================================

    public Map<String, Object> approveTimesheet(Long id, String comments) {
        return publishTimesheetEvent(id, "APPROVED", comments);
    }

    public Map<String, Object> rejectTimesheet(Long id, String comments) {
        return publishTimesheetEvent(id, "REJECTED", comments);
    }

    private Map<String, Object> publishTimesheetEvent(Long id, String status, String comments) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("status", status);
        payload.put("comments", comments != null ? comments : "");

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TIMESHEET_ROUTING_KEY, payload);
        return Map.of("message", "Action requested asynchronously. The Timesheet Service will process this in the background.", "status", "QUEUED");
    }

    public Map<String, Object> approveLeave(Long id, String comments, String approverEmployeeCode, String approverRoles) {
        validateLeaveApprover(id, approverEmployeeCode, parseRoles(approverRoles));
        return publishLeaveEvent(id, "APPROVED", comments);
    }

    public Map<String, Object> rejectLeave(Long id, String comments, String approverEmployeeCode, String approverRoles) {
        validateLeaveApprover(id, approverEmployeeCode, parseRoles(approverRoles));
        return publishLeaveEvent(id, "REJECTED", comments);
    }

    private Map<String, Object> publishLeaveEvent(Long id, String status, String comments) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("status", status);
        payload.put("comments", comments != null ? comments : "");

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.LEAVE_ROUTING_KEY, payload);
        return Map.of("message", "Action requested asynchronously. The Leave Service will process this in the background.", "status", "QUEUED");
    }

    private void validateLeaveApprover(Long leaveId, String approverEmployeeCode, Set<String> approverRoles) {
        if (approverEmployeeCode == null || approverEmployeeCode.isBlank()) {
            throw new BadRequestException("Approver employee code is required");
        }
        if (!approverRoles.contains("ROLE_ADMIN") && !approverRoles.contains("ROLE_MANAGER")) {
            throw new BadRequestException("Only admins or managers can approve leave requests");
        }

        Map<String, Object> leaveRequest = findPendingLeave(leaveId);
        String requesterEmployeeCode = stringValue(leaveRequest.get("employeeCode"));
        if (requesterEmployeeCode == null || requesterEmployeeCode.isBlank()) {
            throw new BadRequestException("Leave request does not include requester employee code");
        }
        if (approverEmployeeCode.equalsIgnoreCase(requesterEmployeeCode)) {
            throw new BadRequestException("You cannot approve or reject your own leave request");
        }

        Set<String> requesterRoles = getUserRoles(requesterEmployeeCode);
        if (requesterRoles.contains("ROLE_ADMIN") && !approverRoles.contains("ROLE_MANAGER")) {
            throw new BadRequestException("Admin leave requests must be approved by a manager");
        }
    }

    private Map<String, Object> findPendingLeave(Long leaveId) {
        return getPendingLeaves().stream()
                .filter(item -> leaveId.equals(longValue(item.get("id"))))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Pending leave request not found with id: " + leaveId));
    }

    private Set<String> getUserRoles(String employeeCode) {
        Map<String, Object> user = identityClient.getUserSummary(employeeCode).getBody();
        if (user == null) {
            return Set.of();
        }
        return rolesFromObject(user.get("roles"));
    }

    private Set<String> parseRoles(String roles) {
        if (roles == null || roles.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .collect(Collectors.toSet());
    }

    private Set<String> rolesFromObject(Object roles) {
        if (roles instanceof Collection<?> collection) {
            return collection.stream().map(Object::toString).collect(Collectors.toSet());
        }
        if (roles == null) {
            return Set.of();
        }
        return parseRoles(roles.toString());
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

		
	
}
