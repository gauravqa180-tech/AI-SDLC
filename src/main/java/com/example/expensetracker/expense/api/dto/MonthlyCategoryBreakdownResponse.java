package com.example.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlyCategoryBreakdownResponse(
        YearMonth month,
        BigDecimal total,
        List<CategoryBreakdownItem> byCategory
) {
}
