package com.leavemanagement.adminservice.service;

import com.leavemanagement.adminservice.client.LeaveClient;
import com.leavemanagement.adminservice.client.TimesheetClient;
import com.leavemanagement.adminservice.config.RabbitMQConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminApprovalService {

    @Autowired
    private TimesheetClient timesheetClient;

    @Autowired
    private LeaveClient leaveClient;

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

    public Map<String, Object> approveLeave(Long id, String comments) {
        return publishLeaveEvent(id, "APPROVED", comments);
    }

    public Map<String, Object> rejectLeave(Long id, String comments) {
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
}
