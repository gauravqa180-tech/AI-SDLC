package com.ai.sdlc.expensetracker.api.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyTotalResponse(
        YearMonth month,
        BigDecimal total
) {
}
