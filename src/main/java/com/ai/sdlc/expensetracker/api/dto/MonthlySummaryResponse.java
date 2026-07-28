package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        String month,
        BigDecimal total,
        List<CategoryTotalResponse> byCategory
) {
}
