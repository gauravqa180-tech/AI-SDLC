package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        int year,
        int month,
        BigDecimal total
) {
}
