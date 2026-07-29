package com.example.expensetracker.budget.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record BudgetStatusResponse(
        String month,
        BigDecimal overallBudget,
        BigDecimal overallSpent,
        BigDecimal overallRemaining,
        Integer overallPercentUsed,
        List<Alert> alerts,
        List<CategoryStatus> categories
) {
    public record CategoryStatus(
            String category,
            BigDecimal budget,
            BigDecimal spent,
            BigDecimal remaining,
            Integer percentUsed
    ) {
    }

    public record Alert(
            String level,
            String message
    ) {
    }
}
