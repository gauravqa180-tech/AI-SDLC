package com.ai.sdlc.expensetracker.insights.api.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlyDashboardResponse(
        YearMonth month,
        BigDecimal monthlyTotal,
        List<CategoryTotalResponse> totalsByCategory,
        BudgetSummary budget
) {
    public record BudgetSummary(
            BigDecimal monthlyBudget,
            BigDecimal monthlyRemaining,
            Integer monthlyPercentUsed,
            List<CategoryBudgetLine> categoryBudgets,
            List<BudgetAlert> alerts
    ) {
    }

    public record CategoryBudgetLine(
            String category,
            BigDecimal budget,
            BigDecimal spent,
            BigDecimal remaining,
            Integer percentUsed
    ) {
    }

    public record BudgetAlert(
            String scope,
            String category,
            String level,
            String message
    ) {
    }
}
