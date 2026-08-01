package com.example.expensetracker.api.dto;

import java.math.BigDecimal;

public record BudgetProgressCategoryItem(
        String category,
        BigDecimal budget,
        BigDecimal spent,
        BigDecimal remaining,
        String status
) {
}
