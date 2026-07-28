package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String category,
        String note,
        Instant deletedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
