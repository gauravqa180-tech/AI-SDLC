package com.ai.sdlc.expensetracker.api.dto;

import com.ai.sdlc.expensetracker.domain.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        ExpenseCategory category,
        String note
) {
}
