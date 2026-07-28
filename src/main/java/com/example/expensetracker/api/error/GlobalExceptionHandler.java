package com.example.expensetracker.api.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI(), List.of());
    }

    private ApiErrorResponse.FieldViolation toViolation(FieldError fe) {
        return new ApiErrorResponse.FieldViolation(fe.getField(), fe.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status,
                                                  String message,
                                                  String path,
                                                  List<ApiErrorResponse.FieldViolation> violations) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                violations
        );
        return ResponseEntity.status(status).body(body);
    }
}
