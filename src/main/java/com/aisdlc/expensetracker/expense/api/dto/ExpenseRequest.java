package com.aisdlc.expensetracker.expense.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
        @Digits(integer = 11, fraction = 2, message = "amount must have up to 11 integer digits and 2 fraction digits")
        BigDecimal amount,

        @NotNull(message = "date is required")
        LocalDate date,

        @NotBlank(message = "category is required")
        @Size(max = 64, message = "category must be <= 64 chars")
        String category,

        @Size(max = 255, message = "note must be <= 255 chars")
        String note
) {
}
