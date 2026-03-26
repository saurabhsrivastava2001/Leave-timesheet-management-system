package com.leavemanagement.timesheetservice.listener;

import com.leavemanagement.timesheetservice.service.TimesheetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TimesheetApprovalListenerTest {

    @Mock
    private TimesheetService timesheetService;

    @InjectMocks
    private TimesheetApprovalListener listener;

    @Test
    void testHandleTimesheetApproval_Success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", 1);
        payload.put("status", "APPROVED");
        payload.put("comments", "Looks good");

        listener.handleTimesheetApproval(payload);

        verify(timesheetService, times(1)).updateStatus(1L, "APPROVED", "Looks good");
    }

    @Test
    void testHandleTimesheetApproval_Exception() {
        Map<String, Object> payload = new HashMap<>();
        // missing fields trigger NullPointerException in the try block
        listener.handleTimesheetApproval(payload);
        verify(timesheetService, never()).updateStatus(any(), any(), any());
    }
}
