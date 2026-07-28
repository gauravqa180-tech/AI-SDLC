package com.ai.sdlc.expensetracker.budget.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BudgetResponse(
        Long id,
        int year,
        int month,
        BigDecimal overallAmount,
        List<CategoryBudgetResponse> categoryBudgets,
        Instant createdAt,
        Instant updatedAt
) {
    public record CategoryBudgetResponse(Long categoryId, String categoryName, BigDecimal amount) {}
}
