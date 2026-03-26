package com.leavemanagement.timesheetservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leavemanagement.timesheetservice.dto.TimesheetDto;
import com.leavemanagement.timesheetservice.dto.TimesheetEntryDto;
import com.leavemanagement.timesheetservice.service.TimesheetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimesheetController.class)
public class TimesheetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimesheetService timesheetService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetTimesheet() throws Exception {
        TimesheetDto dto = new TimesheetDto();
        dto.setId(1L);
        dto.setEmployeeCode("EMP001");
        dto.setWeekStartDate(LocalDate.of(2025, 1, 6));
        dto.setStatus("DRAFT");
        dto.setEntries(new ArrayList<>());

        when(timesheetService.getTimesheet("EMP001", LocalDate.of(2025, 1, 6))).thenReturn(dto);

        mockMvc.perform(get("/api/timesheet/weeks/2025-01-06")
                .header("X-Employee-Code", "EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.employeeCode").value("EMP001"));
    }

    @Test
    void testSaveTimesheet() throws Exception {
        TimesheetDto requestDto = new TimesheetDto();
        requestDto.setWeekStartDate(LocalDate.of(2025, 1, 6));

        TimesheetEntryDto entry = new TimesheetEntryDto();
        entry.setProjectCode("PROJ01");
        entry.setWorkDate(LocalDate.of(2025, 1, 6));
        entry.setHours(8.0);
        entry.setTaskSummary("Coding");
        requestDto.setEntries(List.of(entry));

        TimesheetDto responseDto = new TimesheetDto();
        responseDto.setId(1L);
        responseDto.setStatus("DRAFT");
        responseDto.setEmployeeCode("EMP001");

        when(timesheetService.saveOrUpdateTimesheet(any(TimesheetDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/timesheet/entries")
                .header("X-Employee-Code", "EMP001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void testSubmitTimesheet() throws Exception {
        TimesheetDto dto = new TimesheetDto();
        dto.setId(1L);
        dto.setStatus("SUBMITTED");

        when(timesheetService.submitTimesheet("EMP001", LocalDate.of(2025, 1, 6))).thenReturn(dto);

        mockMvc.perform(post("/api/timesheet/weeks/2025-01-06/submit")
                .header("X-Employee-Code", "EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void testGetPendingApprovals() throws Exception {
        TimesheetDto dto = new TimesheetDto();
        dto.setStatus("SUBMITTED");
        when(timesheetService.getPendingApprovals()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/timesheet/pending-approvals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"));
    }

    @Test
    void testUpdateStatus() throws Exception {
        TimesheetDto dto = new TimesheetDto();
        dto.setId(1L);
        dto.setStatus("APPROVED");

        when(timesheetService.updateStatus(eq(1L), eq("APPROVED"), eq("Looks good"))).thenReturn(dto);

        mockMvc.perform(put("/api/timesheet/1/status")
                .param("status", "APPROVED")
                .param("comments", "Looks good"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
