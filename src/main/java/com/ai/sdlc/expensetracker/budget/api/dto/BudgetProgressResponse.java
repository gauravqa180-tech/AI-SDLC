package com.ai.sdlc.expensetracker.budget.api.dto;

import java.math.BigDecimal;

public record BudgetProgressResponse(
        String month,
        String category,
        BigDecimal budgetAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BigDecimal percentUsed
) {
}
