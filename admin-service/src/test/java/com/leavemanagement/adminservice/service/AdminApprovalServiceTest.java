package com.leavemanagement.adminservice.service;

import com.leavemanagement.adminservice.client.LeaveClient;
import com.leavemanagement.adminservice.client.TimesheetClient;
import com.leavemanagement.adminservice.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminApprovalServiceTest {

    @Mock
    private TimesheetClient timesheetClient;

    @Mock
    private LeaveClient leaveClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AdminApprovalService adminApprovalService;

    @Test
    void testGetPendingTimesheets() {
        when(timesheetClient.getPendingTimesheets()).thenReturn(ResponseEntity.ok(Collections.singletonList(Map.of("id", 1))));
        List<Map<String, Object>> result = adminApprovalService.getPendingTimesheets();
        assertEquals(1, result.size());
    }

    @Test
    void testFallbackGetPendingTimesheets() {
        List<Map<String, Object>> result = adminApprovalService.fallbackGetPendingTimesheets(new RuntimeException("Test Error"));
        assertTrue(result.get(0).containsKey("error"));
    }

    @Test
    void testGetPendingLeaves() {
        when(leaveClient.getPendingLeaveRequests()).thenReturn(ResponseEntity.ok(Collections.singletonList(Map.of("id", 1))));
        List<Map<String, Object>> result = adminApprovalService.getPendingLeaves();
        assertEquals(1, result.size());
    }

    @Test
    void testFallbackGetPendingLeaves() {
        List<Map<String, Object>> result = adminApprovalService.fallbackGetPendingLeaves(new RuntimeException("Test Error"));
        assertTrue(result.get(0).containsKey("error"));
    }

    @Test
    void testApproveTimesheet() {
        Map<String, Object> result = adminApprovalService.approveTimesheet(1L, "OK");
        assertEquals("QUEUED", result.get("status"));
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.TIMESHEET_ROUTING_KEY), any(Map.class));
    }

    @Test
    void testRejectTimesheet() {
        Map<String, Object> result = adminApprovalService.rejectTimesheet(1L, null);
        assertEquals("QUEUED", result.get("status"));
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.TIMESHEET_ROUTING_KEY), any(Map.class));
    }

    @Test
    void testApproveLeave() {
        Map<String, Object> result = adminApprovalService.approveLeave(1L, "OK");
        assertEquals("QUEUED", result.get("status"));
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.LEAVE_ROUTING_KEY), any(Map.class));
    }

    @Test
    void testRejectLeave() {
        Map<String, Object> result = adminApprovalService.rejectLeave(1L, "No");
        assertEquals("QUEUED", result.get("status"));
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.LEAVE_ROUTING_KEY), any(Map.class));
    }
}
