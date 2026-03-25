package com.leavemanagement.leaveservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<Map<String, String>> response = handler.handleResourceNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().get("error"));
    }

    @Test
    void testHandleBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad input");
        ResponseEntity<Map<String, String>> response = handler.handleBadRequestException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad input", response.getBody().get("error"));
    }

    @Test
    void testHandleGlobalException() {
        Exception ex = new Exception("Server down");
        ResponseEntity<Map<String, String>> response = handler.handleGlobalException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred: Server down", response.getBody().get("error"));
    }
}
