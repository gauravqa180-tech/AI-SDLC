package com.example.expensetracker.expense.api.dto;

import com.example.expensetracker.expense.domain.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        ExpenseCategory category,
        String note,
        Long version
) {
}
