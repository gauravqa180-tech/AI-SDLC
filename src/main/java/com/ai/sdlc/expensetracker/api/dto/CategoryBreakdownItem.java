package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;

public record CategoryBreakdownItem(
        String category,
        BigDecimal total
) {
}
