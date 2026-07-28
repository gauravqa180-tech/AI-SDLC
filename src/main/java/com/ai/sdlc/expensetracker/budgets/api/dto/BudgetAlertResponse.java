package com.ai.sdlc.expensetracker.budgets.api.dto;

import java.math.BigDecimal;

public record BudgetAlertResponse(
        String month,
        String category,
        String type,
        BigDecimal budgetAmount,
        BigDecimal spentAmount,
        BigDecimal ratio
) {
}
