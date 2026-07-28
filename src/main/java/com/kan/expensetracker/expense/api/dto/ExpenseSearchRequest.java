package com.kan.expensetracker.expense.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseSearchRequest(
        LocalDate from,
        LocalDate to,
        Long categoryId,
        @DecimalMin(value = "0.00", message = "minAmount must be >= 0")
        BigDecimal minAmount,
        @DecimalMin(value = "0.00", message = "maxAmount must be >= 0")
        BigDecimal maxAmount,
        String q,
        @Pattern(regexp = "date|amount", message = "sortBy must be 'date' or 'amount'")
        String sortBy,
        @Pattern(regexp = "asc|desc", message = "sortDir must be 'asc' or 'desc'")
        String sortDir
) {
}
