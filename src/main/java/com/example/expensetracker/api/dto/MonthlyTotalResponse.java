package com.example.expensetracker.api.dto;

import java.math.BigDecimal;

public record MonthlyTotalResponse(
        int year,
        int month,
        BigDecimal total
) {
}
