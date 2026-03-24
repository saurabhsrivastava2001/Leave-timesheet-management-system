package com.leavemanagement.leaveservice.repository;

import com.leavemanagement.leaveservice.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeCode(String employeeCode);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeCode = :employeeCode " +
           "AND lr.status IN ('SUBMITTED', 'APPROVED') " +
           "AND ((lr.startDate BETWEEN :startDate AND :endDate) OR (lr.endDate BETWEEN :startDate AND :endDate))")
    List<LeaveRequest> findOverlappingRequests(@Param("employeeCode") String employeeCode,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}
