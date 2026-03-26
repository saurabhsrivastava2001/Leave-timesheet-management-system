package com.leavemanagement.timesheetservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

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
    void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("timesheetDto", "weekStartDate", "Week start date is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Week start date is required", response.getBody().get("weekStartDate"));
    }

    @Test
    void testHandleGlobalException() {
        Exception ex = new Exception("Server down");
        ResponseEntity<Map<String, String>> response = handler.handleGlobalException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred: Server down", response.getBody().get("error"));
    }
}
