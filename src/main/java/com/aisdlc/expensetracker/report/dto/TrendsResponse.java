package com.aisdlc.expensetracker.report.dto;

import java.util.List;

public record TrendsResponse(int months, List<MonthlyTrendItem> items) {
}
