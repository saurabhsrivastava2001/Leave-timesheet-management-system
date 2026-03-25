package com.leavemanagement.identityservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leavemanagement.identityservice.dto.AuthResponse;
import com.leavemanagement.identityservice.dto.LoginRequest;
import com.leavemanagement.identityservice.dto.SignupRequest;
import com.leavemanagement.identityservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass Spring Security for unit testing the controller
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterUser() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setEmployeeCode("EMP123");
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(authService.registerUser(any(SignupRequest.class))).thenReturn("Registration successful");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Registration successful"));
    }

    @Test
    void testAuthenticateUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("EMP123");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse("mock_token", "EMP123", "John Doe", Collections.singleton("ROLE_EMPLOYEE"));

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock_token"))
                .andExpect(jsonPath("$.employeeCode").value("EMP123"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
