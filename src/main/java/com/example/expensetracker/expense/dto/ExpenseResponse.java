package com.example.expensetracker.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate expenseDate,
        CategorySummary category,
        String note
) {
    public record CategorySummary(Long id, String name) {
    }
}
