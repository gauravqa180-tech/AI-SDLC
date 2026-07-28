package com.aisdlc.expensetracker.budget.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record BudgetProgressResponse(
        YearMonth month,
        Progress overall,
        List<Progress> byCategory,
        List<String> alerts
) {
    public record Progress(
            String type,
            Long categoryId,
            String categoryName,
            BigDecimal budget,
            BigDecimal spent,
            BigDecimal remaining,
            int percent
    ) {}
}
