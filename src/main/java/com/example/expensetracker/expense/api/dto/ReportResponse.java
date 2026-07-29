package com.example.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal total,
        List<CategoryTotalResponse> byCategory
) {
}
