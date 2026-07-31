package com.example.expensetracker.expenses.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySummaryDto(YearMonth month, BigDecimal total) {
}
