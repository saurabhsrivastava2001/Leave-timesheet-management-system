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
            Long id = Long.valueOf(payload.get("id").toString());
            String status = (String) payload.get("status");
            String comments = (String) payload.get("comments");

            System.out.println(">>> [RabbitMQ CONSUMER] Processing Async Leave Approval | ID: " + id + " | Status: " + status);
            leaveService.updateLeaveStatus(id, status, comments);
            System.out.println("<<< [RabbitMQ CONSUMER] Successfully processed Leave ID: " + id);
        } catch (Exception e) {
            System.err.println("!!! [RabbitMQ ERROR] Failed to process leave async event: " + e.getMessage());
        }
    }
}
