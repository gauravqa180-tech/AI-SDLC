package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;

public record MonthlyBreakdownItemResponse(
        String category,
        BigDecimal total
) {
}
