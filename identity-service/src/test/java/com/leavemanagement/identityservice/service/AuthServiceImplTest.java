package com.leavemanagement.identityservice.service;

import com.leavemanagement.identityservice.dto.AuthResponse;
import com.leavemanagement.identityservice.dto.LoginRequest;
import com.leavemanagement.identityservice.dto.SignupRequest;
import com.leavemanagement.identityservice.entity.User;
import com.leavemanagement.identityservice.exception.BadRequestException;
import com.leavemanagement.identityservice.exception.ResourceNotFoundException;
import com.leavemanagement.identityservice.repository.UserRepository;
import com.leavemanagement.identityservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmployeeCode("EMP123");
        sampleUser.setName("John Doe");
        sampleUser.setEmail("john@example.com");
        sampleUser.setPassword("encoded_password");
        sampleUser.setRoles(Collections.singleton("ROLE_EMPLOYEE"));
        sampleUser.setActive(true);
    }

    @Test
    void testRegisterUser_Success() {
        SignupRequest request = new SignupRequest();
        request.setEmployeeCode("EMP123");
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByEmployeeCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        String result = authService.registerUser(request);

        assertEquals("Registration successful", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailExists() {
        SignupRequest request = new SignupRequest();
        request.setEmail("john@example.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmployeeCodeExists() {
        SignupRequest request = new SignupRequest();
        request.setEmail("john@example.com");
        request.setEmployeeCode("EMP123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByEmployeeCode(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticateUser_Success_ByEmail() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(tokenProvider.generateToken(anyString(), any())).thenReturn("mock_jwt_token");

        AuthResponse response = authService.authenticateUser(request);

        assertNotNull(response);
        assertEquals("mock_jwt_token", response.getToken());
        assertEquals("EMP123", response.getEmployeeCode());
        assertEquals("John Doe", response.getName());
    }

    @Test
    void testAuthenticateUser_Success_ByEmployeeCode() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("EMP123");
        request.setPassword("password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeCode(anyString())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(tokenProvider.generateToken(anyString(), any())).thenReturn("mock_jwt_token");

        AuthResponse response = authService.authenticateUser(request);

        assertNotNull(response);
        assertEquals("mock_jwt_token", response.getToken());
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("unknown@example.com");
        request.setPassword("pass");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeCode(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.authenticateUser(request));
    }

    @Test
    void testAuthenticateUser_InvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("john@example.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.authenticateUser(request));
    }

    @Test
    void testAuthenticateUser_AccountInactive() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("john@example.com");
        request.setPassword("password123");

        sampleUser.setActive(false);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.authenticateUser(request));
    }
}
