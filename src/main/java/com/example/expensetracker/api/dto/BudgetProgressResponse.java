package com.example.expensetracker.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record BudgetProgressResponse(
        String month,
        BigDecimal overallBudget,
        BigDecimal overallSpent,
        BigDecimal overallRemaining,
        String overallStatus,
        List<BudgetProgressCategoryItem> categories,
        int warnThresholdPercent,
        int overThresholdPercent
) {
}
