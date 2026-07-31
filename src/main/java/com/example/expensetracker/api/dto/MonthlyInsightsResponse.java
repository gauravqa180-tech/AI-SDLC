package com.example.expensetracker.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyInsightsResponse(
        String month,
        BigDecimal monthlyTotal,
        List<CategoryTotalResponse> byCategory
) {
}
