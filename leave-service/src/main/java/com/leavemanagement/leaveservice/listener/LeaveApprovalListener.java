package com.leavemanagement.leaveservice.listener;

import com.leavemanagement.leaveservice.config.RabbitMQConfig;
import com.leavemanagement.leaveservice.service.LeaveService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LeaveApprovalListener {

    @Autowired
    private LeaveService leaveService;

    @RabbitListener(queues = RabbitMQConfig.LEAVE_QUEUE)
    public void handleLeaveApproval(Map<String, Object> payload) {
        try {
            Long id = readId(payload);
            String status = readStatus(payload);
            String comments = (String) payload.get("comments");

            System.out.println(">>> [RabbitMQ CONSUMER] Processing Async Leave Approval | ID: " + id + " | Status: " + status);
            leaveService.updateLeaveStatus(id, status, comments);
            System.out.println("<<< [RabbitMQ CONSUMER] Successfully processed Leave ID: " + id);
        } catch (Exception e) {
            System.err.println("!!! [RabbitMQ ERROR] Failed to process leave async event: " + e.getMessage());
        }
    }

    private Long readId(Map<String, Object> payload) {
        Object id = payload.get("id");
        if (id == null) {
            throw new IllegalArgumentException("Missing leave request id");
        }
        return Long.valueOf(id.toString());
    }

    private String readStatus(Map<String, Object> payload) {
        Object status = payload.get("status");
        if (status == null || status.toString().isBlank()) {
            throw new IllegalArgumentException("Missing leave approval status");
        }
        return status.toString();
    }
}
