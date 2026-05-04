package com.leavemanagement.identityservice.service;

import com.leavemanagement.identityservice.dto.AuthResponse;
import com.leavemanagement.identityservice.dto.LoginRequest;
import com.leavemanagement.identityservice.dto.SignupRequest;
import com.leavemanagement.identityservice.dto.UserSummaryResponse;

public interface AuthService {
    String registerUser(SignupRequest signupRequest);
    AuthResponse authenticateUser(LoginRequest loginRequest);
    UserSummaryResponse getUserSummary(String employeeCode);
}
