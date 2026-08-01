package com.aisdlc.expensetracker.expense.api.dto;

import java.math.BigDecimal;

public record MonthlyTotalResponse(
        int year,
        int month,
        BigDecimal total
) {
}
