package com.example.expensetracker.expense.api.dto;

import java.math.BigDecimal;

public record CategoryBreakdownItem(
        String category,
        BigDecimal total
) {
}
