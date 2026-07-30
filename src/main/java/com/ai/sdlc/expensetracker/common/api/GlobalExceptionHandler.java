package com.ai.sdlc.expensetracker.common.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<ErrorResponse.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
        .map(this::toViolation)
        .toList();

    ErrorResponse body = new ErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "Validation failed",
        req.getRequestURI(),
        violations
    );
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    ErrorResponse body = new ErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        ex.getMessage(),
        req.getRequestURI(),
        List.of()
    );
    return ResponseEntity.status(status).body(body);
  }

  private ErrorResponse.FieldViolation toViolation(FieldError fe) {
    return new ErrorResponse.FieldViolation(fe.getField(), fe.getDefaultMessage());
  }
}
