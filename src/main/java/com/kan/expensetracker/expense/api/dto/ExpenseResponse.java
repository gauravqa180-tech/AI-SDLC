package com.kan.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String category,
        String note,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
