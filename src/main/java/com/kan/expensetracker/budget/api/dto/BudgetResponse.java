package com.kan.expensetracker.budget.api.dto;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        int year,
        int month,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        BigDecimal warnThreshold,
        BigDecimal exceedThreshold
) {
}
