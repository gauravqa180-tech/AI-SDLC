package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;

public record MonthlyCategoryBreakdownItem(
        String category,
        BigDecimal total
) {
}
