package com.example.expensetracker.common.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
        .map(this::toViolation)
        .toList();

    ApiError body = new ApiError(
        OffsetDateTime.now(),
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "Validation failed",
        req.getRequestURI(),
        violations
    );
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
    ApiError body = new ApiError(
        OffsetDateTime.now(),
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        ex.getMessage(),
        req.getRequestURI(),
        List.of()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  private ApiError.FieldViolation toViolation(FieldError fe) {
    return new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage());
  }
}
