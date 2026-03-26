package com.leavemanagement.timesheetservice.model;

import com.leavemanagement.timesheetservice.dto.TimesheetDto;
import com.leavemanagement.timesheetservice.dto.TimesheetEntryDto;
import com.leavemanagement.timesheetservice.entity.Project;
import com.leavemanagement.timesheetservice.entity.Timesheet;
import com.leavemanagement.timesheetservice.entity.TimesheetEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    @Test
    void testTimesheetDtoCoverage() {
        TimesheetDto dto = new TimesheetDto();
        dto.setId(1L);
        dto.setEmployeeCode("EMP001");
        dto.setWeekStartDate(LocalDate.of(2025, 1, 6));
        dto.setStatus("DRAFT");
        dto.setManagerComments("Good work");
        dto.setEntries(new ArrayList<>());

        assertEquals(1L, dto.getId());
        assertEquals("EMP001", dto.getEmployeeCode());
        assertEquals(LocalDate.of(2025, 1, 6), dto.getWeekStartDate());
        assertEquals("DRAFT", dto.getStatus());
        assertEquals("Good work", dto.getManagerComments());
        assertNotNull(dto.getEntries());
    }

    @Test
    void testTimesheetEntryDtoCoverage() {
        TimesheetEntryDto dto = new TimesheetEntryDto();
        dto.setId(1L);
        dto.setProjectCode("PROJ01");
        dto.setWorkDate(LocalDate.of(2025, 1, 6));
        dto.setHours(8.0);
        dto.setTaskSummary("Implemented feature X");

        assertEquals(1L, dto.getId());
        assertEquals("PROJ01", dto.getProjectCode());
        assertEquals(LocalDate.of(2025, 1, 6), dto.getWorkDate());
        assertEquals(8.0, dto.getHours());
        assertEquals("Implemented feature X", dto.getTaskSummary());
    }

    @Test
    void testTimesheetEntityCoverage() {
        Timesheet ts = new Timesheet();
        ts.setId(1L);
        ts.setEmployeeCode("EMP001");
        ts.setWeekStartDate(LocalDate.of(2025, 1, 6));
        ts.setStatus("DRAFT");
        ts.setManagerComments("Review needed");
        ts.setEntries(new ArrayList<>());
        ts.setCreatedOn(LocalDateTime.now());
        ts.setUpdatedOn(LocalDateTime.now());

        assertEquals(1L, ts.getId());
        assertEquals("EMP001", ts.getEmployeeCode());
        assertEquals(LocalDate.of(2025, 1, 6), ts.getWeekStartDate());
        assertEquals("DRAFT", ts.getStatus());
        assertEquals("Review needed", ts.getManagerComments());
        assertNotNull(ts.getEntries());
        assertNotNull(ts.getCreatedOn());
        assertNotNull(ts.getUpdatedOn());

        // Test JPA lifecycle hooks
        ts.prePersist();
        assertNotNull(ts.getCreatedOn());
        assertNotNull(ts.getUpdatedOn());

        ts.preUpdate();
        assertNotNull(ts.getUpdatedOn());

        // Test all-args constructor
        Timesheet ts2 = new Timesheet(2L, "EMP002", LocalDate.now(), "SUBMITTED", "OK", null, LocalDateTime.now(), LocalDateTime.now());
        assertEquals(2L, ts2.getId());
        assertEquals("EMP002", ts2.getEmployeeCode());
    }

    @Test
    void testTimesheetEntryEntityCoverage() {
        Timesheet parent = new Timesheet();
        parent.setId(1L);

        Project project = new Project();
        project.setId(1L);
        project.setProjectCode("PROJ01");

        TimesheetEntry entry = new TimesheetEntry();
        entry.setId(1L);
        entry.setTimesheet(parent);
        entry.setProject(project);
        entry.setWorkDate(LocalDate.of(2025, 1, 6));
        entry.setHours(8.0);
        entry.setTaskSummary("Coding");

        assertEquals(1L, entry.getId());
        assertNotNull(entry.getTimesheet());
        assertNotNull(entry.getProject());
        assertEquals(LocalDate.of(2025, 1, 6), entry.getWorkDate());
        assertEquals(8.0, entry.getHours());
        assertEquals("Coding", entry.getTaskSummary());

        // Test all-args constructor
        TimesheetEntry entry2 = new TimesheetEntry(2L, parent, project, LocalDate.now(), 4.0, "Testing");
        assertEquals(2L, entry2.getId());
    }

    @Test
    void testProjectEntityCoverage() {
        Project p = new Project();
        p.setId(1L);
        p.setProjectCode("PROJ01");
        p.setName("Project Alpha");
        p.setDescription("Main project");
        p.setActive(true);
        p.setCreatedOn(LocalDateTime.now());

        assertEquals(1L, p.getId());
        assertEquals("PROJ01", p.getProjectCode());
        assertEquals("Project Alpha", p.getName());
        assertEquals("Main project", p.getDescription());
        assertTrue(p.isActive());
        assertNotNull(p.getCreatedOn());

        // Test JPA lifecycle hook
        p.prePersist();
        assertNotNull(p.getCreatedOn());

        // Test all-args constructor
        Project p2 = new Project(2L, "PROJ02", "Beta", "Second project", false, LocalDateTime.now());
        assertEquals(2L, p2.getId());
        assertEquals("PROJ02", p2.getProjectCode());
        assertFalse(p2.isActive());
    }
}
