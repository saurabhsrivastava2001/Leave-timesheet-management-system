package com.leavemanagement.timesheetservice.service;

import com.leavemanagement.timesheetservice.dto.TimesheetDto;
import com.leavemanagement.timesheetservice.dto.TimesheetEntryDto;
import com.leavemanagement.timesheetservice.entity.Project;
import com.leavemanagement.timesheetservice.entity.Timesheet;
import com.leavemanagement.timesheetservice.entity.TimesheetEntry;
import com.leavemanagement.timesheetservice.exception.BadRequestException;
import com.leavemanagement.timesheetservice.exception.ResourceNotFoundException;
import com.leavemanagement.timesheetservice.repository.ProjectRepository;
import com.leavemanagement.timesheetservice.repository.TimesheetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TimesheetServiceImplTest {

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TimesheetServiceImpl timesheetService;

    private Timesheet sampleTimesheet;
    private Project sampleProject;
    private LocalDate weekStart;

    @BeforeEach
    void setUp() {
        weekStart = LocalDate.of(2025, 1, 6);

        sampleProject = new Project();
        sampleProject.setId(1L);
        sampleProject.setProjectCode("PROJ01");
        sampleProject.setName("Project Alpha");

        sampleTimesheet = new Timesheet();
        sampleTimesheet.setId(1L);
        sampleTimesheet.setEmployeeCode("EMP001");
        sampleTimesheet.setWeekStartDate(weekStart);
        sampleTimesheet.setStatus("DRAFT");

        // Create entries totalling 40 hours
        List<TimesheetEntry> entries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TimesheetEntry entry = new TimesheetEntry();
            entry.setId((long) (i + 1));
            entry.setTimesheet(sampleTimesheet);
            entry.setProject(sampleProject);
            entry.setWorkDate(weekStart.plusDays(i));
            entry.setHours(8.0);
            entry.setTaskSummary("Day " + (i + 1) + " work");
            entries.add(entry);
        }
        sampleTimesheet.setEntries(entries);
    }

    // ---- getTimesheet ----

    @Test
    void testGetTimesheet_Success() {
        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.of(sampleTimesheet));

        TimesheetDto result = timesheetService.getTimesheet("EMP001", weekStart);
        assertNotNull(result);
        assertEquals("EMP001", result.getEmployeeCode());
        assertEquals("DRAFT", result.getStatus());
        assertEquals(5, result.getEntries().size());
    }

    @Test
    void testGetTimesheet_NotFound() {
        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> timesheetService.getTimesheet("EMP001", weekStart));
    }

    // ---- saveOrUpdateTimesheet ----

    @Test
    void testSaveOrUpdateTimesheet_NewDraft() {
        TimesheetDto dto = new TimesheetDto();
        dto.setEmployeeCode("EMP001");
        dto.setWeekStartDate(weekStart);

        TimesheetEntryDto entryDto = new TimesheetEntryDto();
        entryDto.setProjectCode("PROJ01");
        entryDto.setWorkDate(weekStart);
        entryDto.setHours(8.0);
        entryDto.setTaskSummary("Work");
        dto.setEntries(List.of(entryDto));

        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.empty());
        when(projectRepository.findByProjectCode("PROJ01"))
                .thenReturn(Optional.of(sampleProject));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(sampleTimesheet);

        TimesheetDto result = timesheetService.saveOrUpdateTimesheet(dto);
        assertNotNull(result);
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }

    @Test
    void testSaveOrUpdateTimesheet_CannotEditSubmitted() {
        sampleTimesheet.setStatus("SUBMITTED");

        TimesheetDto dto = new TimesheetDto();
        dto.setEmployeeCode("EMP001");
        dto.setWeekStartDate(weekStart);

        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.of(sampleTimesheet));

        assertThrows(BadRequestException.class,
                () -> timesheetService.saveOrUpdateTimesheet(dto));
    }

    @Test
    void testSaveOrUpdateTimesheet_CannotEditApproved() {
        sampleTimesheet.setStatus("APPROVED");

        TimesheetDto dto = new TimesheetDto();
        dto.setEmployeeCode("EMP001");
        dto.setWeekStartDate(weekStart);

        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.of(sampleTimesheet));

        assertThrows(BadRequestException.class,
                () -> timesheetService.saveOrUpdateTimesheet(dto));
    }

    @Test
    void testSaveOrUpdateTimesheet_ExceedsHoursLimit() {
        TimesheetDto dto = new TimesheetDto();
        dto.setEmployeeCode("EMP001");
        dto.setWeekStartDate(weekStart);

        // Create entries totalling > 60 hours
        List<TimesheetEntryDto> entries = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            TimesheetEntryDto entryDto = new TimesheetEntryDto();
            entryDto.setProjectCode("PROJ01");
            entryDto.setWorkDate(weekStart.plusDays(i % 5));
            entryDto.setHours(8.0); // 8 * 8 = 64 > 60
            entryDto.setTaskSummary("Work");
            entries.add(entryDto);
        }
        dto.setEntries(entries);

        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.empty());
        when(projectRepository.findByProjectCode("PROJ01"))
                .thenReturn(Optional.of(sampleProject));

        assertThrows(BadRequestException.class,
                () -> timesheetService.saveOrUpdateTimesheet(dto));
    }

    @Test
    void testSaveOrUpdateTimesheet_ProjectNotFound() {
        TimesheetDto dto = new TimesheetDto();
        dto.setEmployeeCode("EMP001");
        dto.setWeekStartDate(weekStart);

        TimesheetEntryDto entryDto = new TimesheetEntryDto();
        entryDto.setProjectCode("UNKNOWN");
        entryDto.setWorkDate(weekStart);
        entryDto.setHours(8.0);
        dto.setEntries(List.of(entryDto));

        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.empty());
        when(projectRepository.findByProjectCode("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> timesheetService.saveOrUpdateTimesheet(dto));
    }

    // ---- submitTimesheet ----

    @Test
    void testSubmitTimesheet_Success() {
        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.of(sampleTimesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(sampleTimesheet);

        TimesheetDto result = timesheetService.submitTimesheet("EMP001", weekStart);
        assertNotNull(result);
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }

    @Test
    void testSubmitTimesheet_NotDraftOrRejected() {
        sampleTimesheet.setStatus("APPROVED");

        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.of(sampleTimesheet));

        assertThrows(BadRequestException.class,
                () -> timesheetService.submitTimesheet("EMP001", weekStart));
    }

    @Test
    void testSubmitTimesheet_InsufficientHours() {
        // Set hours to < 40
        sampleTimesheet.getEntries().forEach(e -> e.setHours(2.0)); // 5 * 2 = 10

        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.of(sampleTimesheet));

        assertThrows(BadRequestException.class,
                () -> timesheetService.submitTimesheet("EMP001", weekStart));
    }

    @Test
    void testSubmitTimesheet_NotFound() {
        when(timesheetRepository.findByEmployeeCodeAndWeekStartDate("EMP001", weekStart))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> timesheetService.submitTimesheet("EMP001", weekStart));
    }

    // ---- getPendingApprovals ----

    @Test
    void testGetPendingApprovals() {
        sampleTimesheet.setStatus("SUBMITTED");
        when(timesheetRepository.findByStatus("SUBMITTED"))
                .thenReturn(Collections.singletonList(sampleTimesheet));

        List<TimesheetDto> results = timesheetService.getPendingApprovals();
        assertEquals(1, results.size());
        assertEquals("SUBMITTED", results.get(0).getStatus());
    }

    // ---- updateStatus ----

    @Test
    void testUpdateStatus_Approve() {
        when(timesheetRepository.findById(1L)).thenReturn(Optional.of(sampleTimesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(sampleTimesheet);

        TimesheetDto result = timesheetService.updateStatus(1L, "APPROVED", "Good job");
        assertNotNull(result);
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }

    @Test
    void testUpdateStatus_RejectWithoutComments() {
        when(timesheetRepository.findById(1L)).thenReturn(Optional.of(sampleTimesheet));

        assertThrows(BadRequestException.class,
                () -> timesheetService.updateStatus(1L, "REJECTED", ""));
    }

    @Test
    void testUpdateStatus_RejectWithNullComments() {
        when(timesheetRepository.findById(1L)).thenReturn(Optional.of(sampleTimesheet));

        assertThrows(BadRequestException.class,
                () -> timesheetService.updateStatus(1L, "REJECTED", null));
    }

    @Test
    void testUpdateStatus_NotFound() {
        when(timesheetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> timesheetService.updateStatus(1L, "APPROVED", "Ok"));
    }
}
