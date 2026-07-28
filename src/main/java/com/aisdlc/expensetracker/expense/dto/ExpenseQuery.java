package com.aisdlc.expensetracker.expense.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseQuery(
        String q,
        Long categoryId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String sort
) {
}
