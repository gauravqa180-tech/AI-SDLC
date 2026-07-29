package com.example.expensetracker.expense.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpsertRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount must be > 0")
        @Digits(integer = 10, fraction = 2, message = "amount must have up to 10 integer digits and 2 fractional digits")
        BigDecimal amount,

        @NotNull(message = "expenseDate is required")
        LocalDate expenseDate,

        @Size(max = 100, message = "categoryName must be at most 100 characters")
        String categoryName,

        @Size(max = 500, message = "note must be at most 500 characters")
        String note
) {
}
