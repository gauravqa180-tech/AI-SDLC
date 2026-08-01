package com.example.expensetracker.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        OffsetDateTime timestamp,
        List<FieldViolation> fieldViolations
) {
    public record FieldViolation(String field, String message) {}
}
