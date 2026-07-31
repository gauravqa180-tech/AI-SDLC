package com.example.expensetracker.budgets.dto;

import java.time.YearMonth;
import java.util.List;

public record BudgetsOverviewDto(
        YearMonth month,
        BudgetStatusDto overall,
        List<CategoryBudgetOverviewDto> categories
) {
    public record CategoryBudgetOverviewDto(Long categoryId, String categoryName, BudgetStatusDto status) {}
}
