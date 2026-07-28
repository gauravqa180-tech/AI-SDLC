package com.example.expensetracker.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportResponse(
    String month,
    BigDecimal total,
    List<CategoryTotalResponse> byCategory
) {}
