package com.example.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyTotalResponse(
        LocalDate start,
        LocalDate end,
        BigDecimal total
) {
}
