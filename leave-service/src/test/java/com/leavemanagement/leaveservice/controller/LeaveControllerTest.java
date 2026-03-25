package com.leavemanagement.leaveservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leavemanagement.leaveservice.dto.LeaveBalanceDto;
import com.leavemanagement.leaveservice.dto.LeaveRequestDto;
import com.leavemanagement.leaveservice.service.LeaveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaveController.class)
public class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetLeaveBalances() throws Exception {
        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setLeaveType("SICK");
        when(leaveService.getLeaveBalances("EMP001")).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/leave/balance/EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leaveType").value("SICK"));
    }

    @Test
    void testCreateLeaveRequest() throws Exception {
        LeaveRequestDto requestDto = new LeaveRequestDto();
        requestDto.setLeaveType("SICK");
        requestDto.setStartDate(LocalDate.now().plusDays(1));
        requestDto.setEndDate(LocalDate.now().plusDays(2));
        requestDto.setReason("Sick");

        LeaveRequestDto responseDto = new LeaveRequestDto();
        responseDto.setStatus("SUBMITTED");

        when(leaveService.applyForLeave(eq("EMP001"), any(LeaveRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/leave/requests")
                .header("X-Employee-Code", "EMP001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void testGetLeaveHistory() throws Exception {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setStatus("APPROVED");
        when(leaveService.getLeaveHistory("EMP001")).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/leave/history")
                .header("X-Employee-Code", "EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void testGetTeamCalendar() throws Exception {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setStatus("APPROVED");
        when(leaveService.getTeamCalendar(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/leave/team-calendar")
                .param("startDate", "2023-01-01")
                .param("endDate", "2023-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void testGetPendingApprovals() throws Exception {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setStatus("SUBMITTED");
        when(leaveService.getPendingApprovals()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/leave/pending-approvals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"));
    }

    @Test
    void testUpdateStatus() throws Exception {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setStatus("APPROVED");
        when(leaveService.updateLeaveStatus(eq(1L), eq("APPROVED"), eq("Ok"))).thenReturn(dto);

        mockMvc.perform(put("/api/leave/1/status")
                .param("status", "APPROVED")
                .param("comments", "Ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
