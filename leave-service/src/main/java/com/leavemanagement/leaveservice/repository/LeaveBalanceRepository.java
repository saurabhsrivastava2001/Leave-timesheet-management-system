package com.leavemanagement.leaveservice.repository;

import com.leavemanagement.leaveservice.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployeeCode(String employeeCode);
    Optional<LeaveBalance> findByEmployeeCodeAndLeaveType(String employeeCode, String leaveType);
}
