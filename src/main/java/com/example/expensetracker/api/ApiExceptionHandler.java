package com.example.expensetracker.api;

import com.example.expensetracker.service.ConflictException;
import com.example.expensetracker.service.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<?> notFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(problem(HttpStatus.NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<?> conflict(ConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(problem(HttpStatus.CONFLICT, ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest().body(Map.of(
        "timestamp", Instant.now().toString(),
        "status", 400,
        "error", "Bad Request",
        "message", "Validation failed",
        "fieldErrors", ex.getBindingResult().getFieldErrors().stream().map(fe -> Map.of(
            "field", fe.getField(),
            "message", fe.getDefaultMessage()
        )).toList()
    ));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<?> validation(ConstraintViolationException ex) {
    return ResponseEntity.badRequest()
        .body(problem(HttpStatus.BAD_REQUEST, ex.getMessage()));
  }

  private Map<String, Object> problem(HttpStatus status, String message) {
    return Map.of(
        "timestamp", Instant.now().toString(),
        "status", status.value(),
        "error", status.getReasonPhrase(),
        "message", message
    );
  }
}
