package com.aisdlc.expensetracker.insights.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyTotal(LocalDate date, BigDecimal total) {
}
