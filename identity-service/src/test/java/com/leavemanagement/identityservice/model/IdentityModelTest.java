package com.leavemanagement.identityservice.model;

import com.leavemanagement.identityservice.dto.AuthResponse;
import com.leavemanagement.identityservice.dto.LoginRequest;
import com.leavemanagement.identityservice.dto.SignupRequest;
import com.leavemanagement.identityservice.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdentityModelTest {

    @Test
    void testAuthResponse() {
        AuthResponse response = new AuthResponse("mock_token", "EMP123", "John Doe", Collections.singleton("ROLE_USER"));
        assertEquals("mock_token", response.getToken());
        assertEquals("EMP123", response.getEmployeeCode());
        assertEquals("John Doe", response.getName());
        assertTrue(response.getRoles().contains("ROLE_USER"));

        response.setToken("new_token");
        response.setEmployeeCode("EMP456");
        response.setName("Jane Doe");
        response.setRoles(Collections.singleton("ROLE_ADMIN"));

        assertEquals("new_token", response.getToken());
        assertEquals("EMP456", response.getEmployeeCode());
        assertEquals("Jane Doe", response.getName());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void testLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("user@example.com");
        request.setPassword("password");

        assertEquals("user@example.com", request.getUsernameOrEmail());
        assertEquals("password", request.getPassword());
    }

    @Test
    void testSignupRequest() {
        SignupRequest request = new SignupRequest();
        request.setEmployeeCode("EMP123");
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password");

        assertEquals("EMP123", request.getEmployeeCode());
        assertEquals("John", request.getName());
        assertEquals("john@example.com", request.getEmail());
        assertEquals("password", request.getPassword());
    }

    @Test
    void testUserEntity() {
        User user = new User();
        user.setId(1L);
        user.setEmployeeCode("EMP123");
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRoles(Collections.singleton("ROLE_EMPLOYEE"));
        user.setActive(true);

        assertEquals(1L, user.getId());
        assertEquals("EMP123", user.getEmployeeCode());
        assertEquals("John", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertTrue(user.getRoles().contains("ROLE_EMPLOYEE"));
        assertTrue(user.isActive());
    }
}
