package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyBreakdownResponse(
        String month,
        BigDecimal total,
        List<MonthlyBreakdownItemResponse> byCategory
) {
}
