package com.example.expensetracker.budget.api.dto;

import java.math.BigDecimal;

public record BudgetProgressResponse(
        String month,
        String category,
        BigDecimal budgetAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        int percentUsed,
        AlertLevel alert
) {
    public enum AlertLevel {
        NONE,
        WARNING_80,
        EXCEEDED_100
    }
}
