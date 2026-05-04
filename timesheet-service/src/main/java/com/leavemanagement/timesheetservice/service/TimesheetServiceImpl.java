package com.leavemanagement.timesheetservice.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leavemanagement.timesheetservice.dto.ProjectSummaryDto;
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
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
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
        LocalDate normalizedWeekStart = normalizeWeekStart(weekStartDate);
        Optional<Timesheet> optionalTimesheet = timesheetRepository.findByEmployeeCodeAndWeekStartDate(employeeCode, normalizedWeekStart);
        if (optionalTimesheet.isEmpty()) {
            throw new ResourceNotFoundException("Timesheet not found for given week");
        }
        return mapToDto(optionalTimesheet.get());
    }

    @Override
    public List<TimesheetDto> getTimesheetHistory(String employeeCode) {
        return timesheetRepository.findByEmployeeCode(employeeCode).stream()
                .sorted(Comparator.comparing(Timesheet::getWeekStartDate).reversed())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectSummaryDto> getActiveProjects() {
        return projectRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(project -> new ProjectSummaryDto(project.getProjectCode(), project.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TimesheetDto submitTimesheet(String employeeCode, LocalDate weekStartDate) {
        LocalDate normalizedWeekStart = normalizeWeekStart(weekStartDate);
        Timesheet timesheet = timesheetRepository.findByEmployeeCodeAndWeekStartDate(employeeCode, normalizedWeekStart)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet not found for given week"));

        if (!"DRAFT".equals(timesheet.getStatus()) && !"REJECTED".equals(timesheet.getStatus())) {
            throw new BadRequestException("Only DRAFT or REJECTED timesheets can be submitted");
        }

        double totalHours = calculateTotalHours(timesheet.getEntries());
        if (timesheet.getEntries() == null || timesheet.getEntries().isEmpty() || totalHours <= 0) {
            throw new BadRequestException("Add at least one valid entry before submitting the timesheet");
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
        dto.setTotalHours(calculateTotalHours(timesheet.getEntries()));

        if (timesheet.getEntries() != null) {
            List<TimesheetEntryDto> entryDtos = timesheet.getEntries().stream().map(entry -> {
                TimesheetEntryDto entryDto = new TimesheetEntryDto();
                entryDto.setId(entry.getId());
                entryDto.setProjectCode(entry.getProject().getProjectCode());
                entryDto.setWorkDate(entry.getWorkDate());
                entryDto.setHours(entry.getHours());
                entryDto.setTaskSummary(entry.getTaskSummary());
                return entryDto;
            }).sorted(Comparator.comparing(TimesheetEntryDto::getWorkDate)
                    .thenComparing(TimesheetEntryDto::getProjectCode))
              .collect(Collectors.toList());
            dto.setEntries(entryDtos);
        }
        return dto;
    }

    @Override
    @Transactional
    public TimesheetDto saveOrUpdateTimesheet(String employeeCode, TimesheetDto timesheetDto) {
        LocalDate normalizedWeekStart = normalizeWeekStart(timesheetDto.getWeekStartDate());
        Timesheet timesheet = timesheetRepository
                .findByEmployeeCodeAndWeekStartDate(employeeCode, normalizedWeekStart)
                .orElse(new Timesheet());

        if ("SUBMITTED".equals(timesheet.getStatus()) || "APPROVED".equals(timesheet.getStatus())) {
            throw new BadRequestException("Submitted or approved timesheets cannot be edited");
        }

        timesheet.setEmployeeCode(employeeCode);
        timesheet.setWeekStartDate(normalizedWeekStart);
        timesheet.setStatus("DRAFT");

        if (timesheet.getEntries() == null) {
            timesheet.setEntries(new ArrayList<>());
        } else {
            timesheet.getEntries().clear();
        }

        if (timesheetDto.getEntries() != null) {
            for (TimesheetEntryDto entryDto : timesheetDto.getEntries()) {
                validateEntry(entryDto, normalizedWeekStart);

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

        double totalHours = calculateTotalHours(timesheet.getEntries());

        if (totalHours > 60) {
            throw new BadRequestException("Total hours for the week exceed allowed limit (60)");
        }

        Timesheet saved = timesheetRepository.save(timesheet);
        return mapToDto(saved);
    }

    private LocalDate normalizeWeekStart(LocalDate date) {
        if (date == null) {
            throw new BadRequestException("Week start date is required");
        }
        return date.with(DayOfWeek.MONDAY);
    }

    private void validateEntry(TimesheetEntryDto entryDto, LocalDate weekStartDate) {
        if (entryDto.getWorkDate() == null) {
            throw new BadRequestException("Each timesheet entry must include a work date");
        }
        if (entryDto.getProjectCode() == null || entryDto.getProjectCode().isBlank()) {
            throw new BadRequestException("Each timesheet entry must include a project");
        }
        if (entryDto.getHours() == null || entryDto.getHours() <= 0 || entryDto.getHours() > 24) {
            throw new BadRequestException("Each timesheet entry must include hours between 0 and 24");
        }

        LocalDate weekEndDate = weekStartDate.plusDays(6);
        if (entryDto.getWorkDate().isBefore(weekStartDate) || entryDto.getWorkDate().isAfter(weekEndDate)) {
            throw new BadRequestException("Work date must fall within the selected week");
        }
    }

    private double calculateTotalHours(List<TimesheetEntry> entries) {
        if (entries == null) {
            return 0;
        }
        return entries.stream()
                .mapToDouble(TimesheetEntry::getHours)
                .sum();
    }
}
