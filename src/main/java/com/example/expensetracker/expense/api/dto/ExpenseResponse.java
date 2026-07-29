package com.example.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String category,
        String note,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt
) {
}
