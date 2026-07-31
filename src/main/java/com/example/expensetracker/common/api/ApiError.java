package com.example.expensetracker.common.api;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> fieldViolations
) {
    public record FieldViolation(String field, String message) {}
}
