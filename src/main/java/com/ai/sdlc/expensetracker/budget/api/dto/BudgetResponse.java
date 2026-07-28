package com.ai.sdlc.expensetracker.budget.api.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record BudgetResponse(
        Long id,
        YearMonth month,
        BigDecimal monthlyBudget,
        List<CategoryBudgetLine> categoryBudgets
) {
    public record CategoryBudgetLine(
            String category,
            BigDecimal budgetAmount
    ) {
    }
}
