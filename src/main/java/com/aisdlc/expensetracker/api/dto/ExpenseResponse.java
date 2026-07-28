package com.aisdlc.expensetracker.api.dto;

import com.aisdlc.expensetracker.domain.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate expenseDate,
        ExpenseCategory category,
        String note,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
