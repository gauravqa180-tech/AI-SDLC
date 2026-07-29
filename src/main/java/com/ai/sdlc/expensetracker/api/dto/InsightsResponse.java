package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InsightsResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal total,
        List<CategoryTotal> categories
) {
    public record CategoryTotal(
            String category,
            BigDecimal total,
            BigDecimal percentOfTotal
    ) {
    }
}
