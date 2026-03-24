package com.leavemanagement.timesheetservice.repository;

import com.leavemanagement.timesheetservice.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    Optional<Timesheet> findByEmployeeCodeAndWeekStartDate(String employeeCode, LocalDate weekStartDate);
    List<Timesheet> findByEmployeeCode(String employeeCode);
    List<Timesheet> findByStatus(String status);
}
