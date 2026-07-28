package com.ai.sdlc.expensetracker.summary.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyCategoryTotalResponse(
        String month,
        BigDecimal monthlyTotal,
        List<CategoryTotal> categories
) {
    public record CategoryTotal(String category, BigDecimal total) {
    }
}
