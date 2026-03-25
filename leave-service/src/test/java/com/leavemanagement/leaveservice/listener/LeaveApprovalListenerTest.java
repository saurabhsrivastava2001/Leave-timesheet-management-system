package com.leavemanagement.leaveservice.listener;

import com.leavemanagement.leaveservice.service.LeaveService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveApprovalListenerTest {

    @Mock
    private LeaveService leaveService;

    @InjectMocks
    private LeaveApprovalListener listener;

    @Test
    void testHandleLeaveApproval_Success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", 1);
        payload.put("status", "APPROVED");
        payload.put("comments", "Ok");

        listener.handleLeaveApproval(payload);

        verify(leaveService, times(1)).updateLeaveStatus(1L, "APPROVED", "Ok");
    }

    @Test
    void testHandleLeaveApproval_Exception() {
        Map<String, Object> payload = new HashMap<>();
        // missing fields trigger NumberFormatException etc.
        listener.handleLeaveApproval(payload);
        verify(leaveService, never()).updateLeaveStatus(any(), any(), any());
    }
}
