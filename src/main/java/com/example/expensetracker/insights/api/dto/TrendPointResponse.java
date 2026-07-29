package com.example.expensetracker.insights.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrendPointResponse(LocalDate date, BigDecimal total) {
}
