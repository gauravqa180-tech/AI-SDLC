package com.ai.sdlc.expensetracker.common.api;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldViolation> violations
) {

  public record FieldViolation(String field, String message) {
  }
}
