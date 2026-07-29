package com.example.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyBreakdownResponse(
        String month,
        BigDecimal total,
        List<CategoryTotalResponse> byCategory
) {
}
