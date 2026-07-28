package com.example.expensetracker.expense.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "amount must have up to 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "date is required")
        LocalDate date,

        @NotBlank(message = "category is required")
        @Size(max = 64, message = "category must be at most 64 characters")
        String category,

        @Size(max = 500, message = "note must be at most 500 characters")
        String note,

        @NotNull(message = "version is required")
        Long version
) {
}
