package com.ai.sdlc.expensetracker.expense.api.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseSearchRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        String category,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String note,
        String sortBy,
        String sortDir
) {
}
