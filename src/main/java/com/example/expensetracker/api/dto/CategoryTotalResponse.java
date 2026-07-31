package com.example.expensetracker.api.dto;

import com.example.expensetracker.domain.ExpenseCategory;

import java.math.BigDecimal;

public record CategoryTotalResponse(
        ExpenseCategory category,
        BigDecimal total
) {
}
