package com.aisdlc.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate expenseDate,
        String category,
        String note
) {
}
