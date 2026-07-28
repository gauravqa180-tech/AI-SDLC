package com.aisdlc.expensetracker.report.dto;

import com.aisdlc.expensetracker.expense.dto.ExpenseResponse;

import java.time.YearMonth;
import java.util.List;

public record HighlightsResponse(
        YearMonth month,
        List<CategoryBreakdownItem> topCategories,
        List<ExpenseResponse> largestExpenses
) {
}
