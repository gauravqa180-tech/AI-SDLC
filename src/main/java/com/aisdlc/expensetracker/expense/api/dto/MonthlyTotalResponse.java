package com.aisdlc.expensetracker.expense.api.dto;

import java.math.BigDecimal;

public record MonthlyTotalResponse(
        String month,
        BigDecimal total
) {
}
