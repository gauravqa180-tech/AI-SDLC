package com.ai.sdlc.expensetracker.budget.api.dto;

import java.math.BigDecimal;

public record BudgetAlertResponse(
    String month,
    String category,
    BigDecimal spend,
    BigDecimal budget,
    int thresholdPercent,
    String message
) {
}
