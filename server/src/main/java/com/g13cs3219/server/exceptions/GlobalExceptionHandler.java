package com.g13cs3219.server.exceptions;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return buildError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(EmptyArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyArgument(EmptyArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        return buildError(ex.getStatusCode().value(), ex.getReason());
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRoleID(InvalidRoleException ex) {
        return buildError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(InvalidUserIDException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidUserID(InvalidUserIDException ex) {
        return buildError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected server error: " + ex.getMessage());
    }

    /**
     * Helper method to build a consistent error response body.
     *
     * @param status the HTTP status code
     * @param message the error message to include in the response
     * @return a ResponseEntity containing the error details
     */
    private ResponseEntity<Map<String, Object>> buildError(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        HttpStatus httpStatus = HttpStatus.resolve(status);
        if (httpStatus != null) {
            body.put("error", httpStatus.getReasonPhrase());
        }
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
