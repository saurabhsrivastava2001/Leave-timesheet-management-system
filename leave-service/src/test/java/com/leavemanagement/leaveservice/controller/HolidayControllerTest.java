package com.leavemanagement.leaveservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leavemanagement.leaveservice.dto.HolidayDto;
import com.leavemanagement.leaveservice.entity.Holiday;
import com.leavemanagement.leaveservice.repository.HolidayRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HolidayController.class)
public class HolidayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HolidayRepository holidayRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllHolidays() throws Exception {
        Holiday h = new Holiday();
        h.setDate(LocalDate.now());
        h.setDescription("New Year");
        when(holidayRepository.findAll()).thenReturn(Collections.singletonList(h));

        mockMvc.perform(get("/api/holidays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("New Year"));
    }

    @Test
    void testAddHoliday() throws Exception {
        HolidayDto dto = new HolidayDto();
        dto.setDate(LocalDate.now());
        dto.setDescription("New Year");

        when(holidayRepository.save(any(Holiday.class))).thenReturn(new Holiday());

        mockMvc.perform(post("/api/holidays")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("New Year"));
    }
}
