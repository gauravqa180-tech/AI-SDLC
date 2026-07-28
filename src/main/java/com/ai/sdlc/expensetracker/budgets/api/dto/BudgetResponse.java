package com.ai.sdlc.expensetracker.budgets.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record BudgetResponse(
        String month,
        BigDecimal overallAmount,
        List<CategoryBudget> categoryBudgets
) {
    public record CategoryBudget(String category, BigDecimal amount) {
    }
}
