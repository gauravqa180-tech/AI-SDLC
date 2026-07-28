package com.example.expensetracker.api.dto;

import java.math.BigDecimal;

public record BudgetProgressResponse(
    Long id,
    String month,
    String category,
    BigDecimal budgetAmount,
    BigDecimal spentAmount,
    BigDecimal percentUsed,
    boolean overspent
) {}
