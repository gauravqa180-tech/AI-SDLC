package com.ai.sdlc.expensetracker.budget.api.dto;

import java.math.BigDecimal;

public record BudgetResponse(
    Long id,
    String month,
    String category,
    BigDecimal amount
) {
}
