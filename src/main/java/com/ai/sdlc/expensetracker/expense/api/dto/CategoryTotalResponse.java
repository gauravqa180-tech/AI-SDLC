package com.ai.sdlc.expensetracker.expense.api.dto;

import java.math.BigDecimal;

public record CategoryTotalResponse(
    String category,
    BigDecimal total
) {
}
