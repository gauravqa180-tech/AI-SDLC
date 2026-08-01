package com.example.expensetracker.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String category,
        String note,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
