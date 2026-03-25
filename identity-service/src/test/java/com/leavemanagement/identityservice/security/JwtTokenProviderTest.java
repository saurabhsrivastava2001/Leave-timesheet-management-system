package com.leavemanagement.identityservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", 86400000L);
    }

    @Test
    void testGenerateTokenAndGetEmployeeCode() {
        String token = tokenProvider.generateToken("EMP123", Collections.singleton("ROLE_EMPLOYEE"));
        assertNotNull(token);

        String employeeCode = tokenProvider.getEmployeeCodeFromJWT(token);
        assertEquals("EMP123", employeeCode);
    }

    @Test
    void testValidateToken_Success() {
        String token = tokenProvider.generateToken("EMP123", Collections.singleton("ROLE_EMPLOYEE"));
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_Failure() {
        assertFalse(tokenProvider.validateToken("invalidToken"));
    }
}
