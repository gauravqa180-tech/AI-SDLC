package com.ai.sdlc.expensetracker.reports.api.dto;

import java.math.BigDecimal;

public record MonthlyTotalResponse(String month, BigDecimal total) {
}
