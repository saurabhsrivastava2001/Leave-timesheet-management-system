package com.leavemanagement.identityservice.repository;

import com.leavemanagement.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmployeeCode(String employeeCode);
    Boolean existsByEmail(String email);
    Boolean existsByEmployeeCode(String employeeCode);
}
