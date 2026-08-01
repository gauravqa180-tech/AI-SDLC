package com.ai.sdlc.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlySummaryResponse(
        YearMonth month,
        BigDecimal total,
        List<CategoryTotal> byCategory
) {
    public record CategoryTotal(String category, BigDecimal total) {
    }
}
