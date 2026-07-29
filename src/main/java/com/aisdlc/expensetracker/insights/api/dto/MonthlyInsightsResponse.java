package com.aisdlc.expensetracker.insights.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyInsightsResponse(
        String month,
        BigDecimal total,
        List<CategoryTotal> byCategory,
        List<DailyTotal> daily
) {
}
