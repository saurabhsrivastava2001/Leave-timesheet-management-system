package com.leavemanagement.leaveservice.controller;

// touch: refresh IDE index

import com.leavemanagement.leaveservice.dto.HolidayDto;
import com.leavemanagement.leaveservice.entity.Holiday;
import com.leavemanagement.leaveservice.repository.HolidayRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/holidays")
@Tag(name = "Holidays", description = "Holiday Master Data Management")
public class HolidayController {

    @Autowired
    private HolidayRepository holidayRepository;

    @GetMapping
    @Operation(summary = "Get all holidays")
    public ResponseEntity<List<HolidayDto>> getAllHolidays() {
        return ResponseEntity.ok(holidayRepository.findAll().stream().map(h -> {
            HolidayDto dto = new HolidayDto();
            dto.setDate(h.getDate());
            dto.setDescription(h.getDescription());
            return dto;
        }).collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Add a new holiday")
    public ResponseEntity<HolidayDto> addHoliday(@RequestBody HolidayDto dto) {
        Holiday holiday = new Holiday();
        holiday.setDate(dto.getDate());
        holiday.setDescription(dto.getDescription());
        holidayRepository.save(holiday);
        return ResponseEntity.ok(dto);
    }
}