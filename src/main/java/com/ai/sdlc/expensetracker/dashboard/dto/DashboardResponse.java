package com.ai.sdlc.expensetracker.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        int year,
        int month,
        BigDecimal totalSpent,
        BigDecimal overallBudget,
        BigDecimal overallRemaining,
        List<CategoryBreakdown> categoryBreakdown
) {
    public record CategoryBreakdown(
            Long categoryId,
            String categoryName,
            BigDecimal spent,
            BigDecimal budget,
            BigDecimal remaining
    ) {}
}
