package com.example.expensetracker.api.dto;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        String month,
        String category,
        BigDecimal amount
) {
}
