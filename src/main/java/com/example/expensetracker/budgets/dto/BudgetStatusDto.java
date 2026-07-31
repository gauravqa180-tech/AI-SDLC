package com.example.expensetracker.budgets.dto;

import java.math.BigDecimal;

public record BudgetStatusDto(
        BigDecimal budgetAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        boolean warning,
        boolean exceeded
) {
}
