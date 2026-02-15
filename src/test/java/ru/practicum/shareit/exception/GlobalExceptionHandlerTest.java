package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returnsErrorResponse() {
        ErrorResponse response = handler.handleNotFound(new NotFoundException("not found"));

        assertEquals("not found", response.getError());
    }

    @Test
    void handleConflict_returnsErrorResponse() {
        ErrorResponse response = handler.handleConflict(new ConflictException("conflict"));

        assertEquals("conflict", response.getError());
    }

    @Test
    void handleForbidden_returnsErrorResponse() {
        ErrorResponse response = handler.handleForbidden(new ForbiddenException("forbidden"));

        assertEquals("forbidden", response.getError());
    }

    @Test
    void handleValidation_returnsErrorResponse() {
        ErrorResponse response = handler.handleValidation(new ValidationException("bad request"));

        assertEquals("bad request", response.getError());
    }

    @Test
    void handleBadRequest_returnsErrorResponse() {
        ErrorResponse response = handler.handleBadRequest(new Exception("bad"));

        assertEquals("bad", response.getError());
    }

    @Test
    void handleRuntime_returnsErrorResponse() {
        ErrorResponse response = handler.handleRuntime(new RuntimeException("runtime"));

        assertEquals("runtime", response.getError());
    }
}
