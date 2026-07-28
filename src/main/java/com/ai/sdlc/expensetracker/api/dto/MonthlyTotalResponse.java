package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;

public record MonthlyTotalResponse(
        String month,
        BigDecimal total
) {
}
