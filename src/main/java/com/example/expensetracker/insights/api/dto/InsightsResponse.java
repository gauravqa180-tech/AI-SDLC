package com.example.expensetracker.insights.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InsightsResponse(
        LocalDate start,
        LocalDate end,
        BigDecimal total,
        BigDecimal previousPeriodTotal,
        BigDecimal delta,
        List<CategoryTotalResponse> byCategory,
        List<TrendPointResponse> dailyTrend
) {
}
