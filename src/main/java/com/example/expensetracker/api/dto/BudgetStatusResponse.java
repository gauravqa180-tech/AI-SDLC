package com.example.expensetracker.api.dto;

import com.example.expensetracker.domain.ExpenseCategory;

import java.math.BigDecimal;

public record BudgetStatusResponse(
        String month,
        ExpenseCategory category,
        BigDecimal budget,
        BigDecimal spent,
        BigDecimal remaining
) {
}
