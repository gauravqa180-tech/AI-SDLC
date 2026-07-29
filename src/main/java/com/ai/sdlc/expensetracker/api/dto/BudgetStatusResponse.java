package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;

public record BudgetStatusResponse(
        String period,
        String category,
        BigDecimal limitAmount,
        BigDecimal warnThresholdPercent,
        BigDecimal spentAmount,
        BigDecimal spentPercent,
        boolean warn,
        boolean exceeded
) {
}
