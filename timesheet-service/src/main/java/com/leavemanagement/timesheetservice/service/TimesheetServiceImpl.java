package com.leavemanagement.timesheetservice.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leavemanagement.timesheetservice.dto.TimesheetDto;
import com.leavemanagement.timesheetservice.dto.TimesheetEntryDto;
import com.leavemanagement.timesheetservice.entity.Project;
import com.leavemanagement.timesheetservice.entity.Timesheet;
import com.leavemanagement.timesheetservice.entity.TimesheetEntry;
import com.leavemanagement.timesheetservice.exception.BadRequestException;
import com.leavemanagement.timesheetservice.exception.ResourceNotFoundException;
import com.leavemanagement.timesheetservice.repository.ProjectRepository;
import com.leavemanagement.timesheetservice.repository.TimesheetRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public TimesheetDto getTimesheet(String employeeCode, LocalDate weekStartDate) {
        Optional<Timesheet> optionalTimesheet = timesheetRepository.findByEmployeeCodeAndWeekStartDate(employeeCode, weekStartDate);
        if (optionalTimesheet.isEmpty()) {
            throw new ResourceNotFoundException("Timesheet not found for given week");
        }
        return mapToDto(optionalTimesheet.get());
    }



    @Override
    @Transactional
    public TimesheetDto submitTimesheet(String employeeCode, LocalDate weekStartDate) {
        Timesheet timesheet = timesheetRepository.findByEmployeeCodeAndWeekStartDate(employeeCode, weekStartDate)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found for given week"));

        if (!"DRAFT".equals(timesheet.getStatus()) && !"REJECTED".equals(timesheet.getStatus())) {
            throw new BadRequestException("Timesheet can only be submitted if it is DRAFT or REJECTED");
        }

        double totalHours = timesheet.getEntries().stream().mapToDouble(TimesheetEntry::getHours).sum();
        if (totalHours < 40) {
            throw new BadRequestException("Minimum 40 hours required to submit a weekly timesheet");
        }

        timesheet.setStatus("SUBMITTED");
        return mapToDto(timesheetRepository.save(timesheet));
    }

    @Override
    public List<TimesheetDto> getPendingApprovals() {
        return timesheetRepository.findByStatus("SUBMITTED").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TimesheetDto updateStatus(Long timesheetId, String status, String managerComments) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found with id: " + timesheetId));

        if ("REJECTED".equals(status) && (managerComments == null || managerComments.isEmpty())) {
            throw new BadRequestException("Manager comments are required when rejecting a timesheet");
        }

        timesheet.setStatus(status);
        timesheet.setManagerComments(managerComments);
        return mapToDto(timesheetRepository.save(timesheet));
    }

    private TimesheetDto mapToDto(Timesheet timesheet) {
        TimesheetDto dto = new TimesheetDto();
        dto.setId(timesheet.getId());
        dto.setEmployeeCode(timesheet.getEmployeeCode());
        dto.setWeekStartDate(timesheet.getWeekStartDate());
        dto.setStatus(timesheet.getStatus());
        dto.setManagerComments(timesheet.getManagerComments());

        if (timesheet.getEntries() != null) {
            List<TimesheetEntryDto> entryDtos = timesheet.getEntries().stream().map(entry -> {
                TimesheetEntryDto entryDto = new TimesheetEntryDto();
                entryDto.setId(entry.getId());
                entryDto.setProjectCode(entry.getProject().getProjectCode());
                entryDto.setWorkDate(entry.getWorkDate());
                entryDto.setHours(entry.getHours());
                entryDto.setTaskSummary(entry.getTaskSummary());
                return entryDto;
            }).collect(Collectors.toList());
            dto.setEntries(entryDtos);
        }
        return dto;
    }

    @Override
    @Transactional
    public TimesheetDto saveOrUpdateTimesheet(TimesheetDto timesheetDto) {
        Timesheet timesheet = timesheetRepository
                .findByEmployeeCodeAndWeekStartDate(timesheetDto.getEmployeeCode(), timesheetDto.getWeekStartDate())
                .orElse(new Timesheet());

        if ("SUBMITTED".equals(timesheet.getStatus()) || "APPROVED".equals(timesheet.getStatus())) {
            throw new BadRequestException("Cannot edit a submitted or approved timesheet");
        }

        timesheet.setEmployeeCode(timesheetDto.getEmployeeCode());
        timesheet.setWeekStartDate(timesheetDto.getWeekStartDate());
        timesheet.setStatus("DRAFT");

        if (timesheet.getEntries() != null) {
            timesheet.getEntries().clear();
        } else {
            timesheet.setEntries(new ArrayList<>());
        }

        if (timesheetDto.getEntries() != null) {
            for (TimesheetEntryDto entryDto : timesheetDto.getEntries()) {
                Project project = projectRepository.findByProjectCode(entryDto.getProjectCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + entryDto.getProjectCode()));

                TimesheetEntry entry = new TimesheetEntry();
                entry.setTimesheet(timesheet);
                entry.setProject(project);
                entry.setWorkDate(entryDto.getWorkDate());
                entry.setHours(entryDto.getHours());
                entry.setTaskSummary(entryDto.getTaskSummary());

                timesheet.getEntries().add(entry);
            }
        }

        double totalHours = timesheet.getEntries().stream()
                .mapToDouble(TimesheetEntry::getHours)
                .sum();

        if (totalHours > 60) {
            throw new BadRequestException("Total hours for the week exceed allowed limit (60)");
        }

        Timesheet saved = timesheetRepository.save(timesheet);
        return mapToDto(saved);
    }
}
