package com.ai.sdlc.expensetracker.insights.api.dto;

import java.math.BigDecimal;

public record CategoryTotalResponse(
        String category,
        BigDecimal total
) {
}
