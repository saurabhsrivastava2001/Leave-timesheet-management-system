package com.leavemanagement.identityservice.service;

// ...existing code...
import com.leavemanagement.identityservice.dto.AuthResponse;
import com.leavemanagement.identityservice.dto.LoginRequest;
import com.leavemanagement.identityservice.dto.SignupRequest;
import com.leavemanagement.identityservice.entity.User;
import com.leavemanagement.identityservice.exception.BadRequestException;
import com.leavemanagement.identityservice.exception.ResourceNotFoundException;
import com.leavemanagement.identityservice.repository.UserRepository;
import com.leavemanagement.identityservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    public String registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        if (userRepository.existsByEmployeeCode(signupRequest.getEmployeeCode())) {
            throw new BadRequestException("Employee code not found or already registered");
        }

        User user = new User();
        user.setEmployeeCode(signupRequest.getEmployeeCode());
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRoles(Collections.singleton("ROLE_EMPLOYEE"));

        userRepository.save(user);
        return "Registration successful";
    }

    @Override
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getUsernameOrEmail());
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmployeeCode(loginRequest.getUsernameOrEmail());
        }

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("Invalid credentials");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new BadRequestException("Account is locked or inactive");
        }

        String jwt = tokenProvider.generateToken(user.getEmployeeCode(), user.getRoles());
        return new AuthResponse(jwt, user.getEmployeeCode(), user.getName(), user.getRoles());
    }
}