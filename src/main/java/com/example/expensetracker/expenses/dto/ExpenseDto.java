package com.example.expensetracker.expenses.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseDto(
        Long id,
        BigDecimal amount,
        LocalDate date,
        Long categoryId,
        String categoryName,
        String note,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
