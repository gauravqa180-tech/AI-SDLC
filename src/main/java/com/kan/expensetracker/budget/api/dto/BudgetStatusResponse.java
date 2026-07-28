package com.kan.expensetracker.budget.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record BudgetStatusResponse(
        int year,
        int month,
        BudgetProgress overall,
        List<BudgetProgress> byCategory,
        List<BudgetAlert> alerts
) {
    public record BudgetProgress(
            Long budgetId,
            Long categoryId,
            String categoryName,
            BigDecimal spent,
            BigDecimal budget,
            BigDecimal remaining
    ) {}

    public record BudgetAlert(
            String scope,
            Long categoryId,
            String categoryName,
            String threshold,
            BigDecimal spent,
            BigDecimal budget
    ) {}
}
