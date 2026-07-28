package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;

public record AlertResponse(
        String month,
        String scope,
        String category,
        Double threshold,
        String message,
        BigDecimal spent,
        BigDecimal budget
) {
}
