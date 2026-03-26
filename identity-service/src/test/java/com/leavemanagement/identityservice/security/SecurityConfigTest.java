package com.leavemanagement.identityservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityConfigTest {

    @Test
    void testPasswordEncoderBean() {
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void testPasswordEncoderEncodes() {
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        String encoded = encoder.encode("password123");
        assertNotNull(encoded);
        assertNotEquals("password123", encoded);
        assertTrue(encoder.matches("password123", encoded));
    }
}
