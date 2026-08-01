package com.example.expensetracker.api.dto;

import java.math.BigDecimal;

public record MonthlyTotalResponse(
        String month,
        BigDecimal total
) {
}
