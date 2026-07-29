package com.example.expensetracker.expense.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "amount must be >= 0")
        @Digits(integer = 17, fraction = 2, message = "amount must be a valid currency amount")
        BigDecimal amount,

        @NotNull(message = "date is required")
        LocalDate date,

        @NotBlank(message = "category is required")
        @Size(max = 100, message = "category must be <= 100 characters")
        String category,

        @Size(max = 500, message = "note must be <= 500 characters")
        String note
) {
}
