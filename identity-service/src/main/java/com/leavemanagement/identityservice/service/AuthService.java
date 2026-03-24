package com.leavemanagement.identityservice.service;

import com.leavemanagement.identityservice.dto.AuthResponse;
import com.leavemanagement.identityservice.dto.LoginRequest;
import com.leavemanagement.identityservice.dto.SignupRequest;

public interface AuthService {
    String registerUser(SignupRequest signupRequest);
    AuthResponse authenticateUser(LoginRequest loginRequest);
}
