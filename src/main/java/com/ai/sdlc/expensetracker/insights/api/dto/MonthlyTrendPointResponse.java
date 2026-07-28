package com.ai.sdlc.expensetracker.insights.api.dto;

import java.math.BigDecimal;

public record MonthlyTrendPointResponse(String month, BigDecimal total) {
}
