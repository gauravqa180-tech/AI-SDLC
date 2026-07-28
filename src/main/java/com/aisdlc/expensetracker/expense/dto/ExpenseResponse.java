package com.aisdlc.expensetracker.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        Long categoryId,
        String categoryName,
        String note
) {
}
