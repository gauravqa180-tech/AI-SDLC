package com.aisdlc.expensetracker.budget.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BudgetResponse(
        Long id,
        YearMonth month,
        String type,
        Long categoryId,
        String categoryName,
        BigDecimal amount
) {
}
