package com.leavemanagement.timesheetservice.listener;

import com.leavemanagement.timesheetservice.config.RabbitMQConfig;
import com.leavemanagement.timesheetservice.service.TimesheetService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TimesheetApprovalListener {

    @Autowired
    private TimesheetService timesheetService;

    @RabbitListener(queues = RabbitMQConfig.TIMESHEET_QUEUE)
    public void handleTimesheetApproval(Map<String, Object> payload) {
        try {
            Long id = readId(payload);
            String status = readStatus(payload);
            String comments = (String) payload.get("comments");

            System.out.println(">>> [RabbitMQ CONSUMER] Processing Async Timesheet Approval | ID: " + id + " | Status: " + status);
            timesheetService.updateStatus(id, status, comments);
            System.out.println("<<< [RabbitMQ CONSUMER] Successfully processed Timesheet ID: " + id);
        } catch (Exception e) {
            System.err.println("!!! [RabbitMQ ERROR] Failed to process timesheet async event: " + e.getMessage());
        }
    }

    private Long readId(Map<String, Object> payload) {
        Object id = payload.get("id");
        if (id == null) {
            throw new IllegalArgumentException("Missing timesheet id");
        }
        return Long.valueOf(id.toString());
    }

    private String readStatus(Map<String, Object> payload) {
        Object status = payload.get("status");
        if (status == null || status.toString().isBlank()) {
            throw new IllegalArgumentException("Missing timesheet approval status");
        }
        return status.toString();
    }
}
