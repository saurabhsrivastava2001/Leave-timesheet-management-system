package com.leavemanagement.adminservice.controller;

import com.leavemanagement.adminservice.service.AdminApprovalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AdminApprovalController.class)
public class AdminApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminApprovalService adminApprovalService;

    @Test
    void testGetPendingTimesheets() throws Exception {
        when(adminApprovalService.getPendingTimesheets()).thenReturn(Collections.singletonList(Map.of("id", 1)));
        mockMvc.perform(get("/api/admin/approvals/timesheets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testApproveTimesheet() throws Exception {
        when(adminApprovalService.approveTimesheet(1L, "OK")).thenReturn(Map.of("status", "QUEUED"));
        mockMvc.perform(post("/api/admin/approvals/timesheets/1/approve").param("comments", "OK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testRejectTimesheet() throws Exception {
        when(adminApprovalService.rejectTimesheet(1L, "No")).thenReturn(Map.of("status", "QUEUED"));
        mockMvc.perform(post("/api/admin/approvals/timesheets/1/reject").param("comments", "No"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testGetPendingLeaves() throws Exception {
        when(adminApprovalService.getPendingLeaves()).thenReturn(Collections.singletonList(Map.of("id", 1)));
        mockMvc.perform(get("/api/admin/approvals/leaves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testApproveLeave() throws Exception {
        when(adminApprovalService.approveLeave(1L, "OK")).thenReturn(Map.of("status", "QUEUED"));
        mockMvc.perform(post("/api/admin/approvals/leaves/1/approve").param("comments", "OK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testRejectLeave() throws Exception {
        when(adminApprovalService.rejectLeave(1L, "No")).thenReturn(Map.of("status", "QUEUED"));
        mockMvc.perform(post("/api/admin/approvals/leaves/1/reject").param("comments", "No"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }
}
