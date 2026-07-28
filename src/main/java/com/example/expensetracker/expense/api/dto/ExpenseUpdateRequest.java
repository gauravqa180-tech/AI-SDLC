package com.example.expensetracker.expense.api.dto;

import com.example.expensetracker.expense.domain.ExpenseCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * For v1, we keep update as full replace (PUT).
 * If patch is needed later, we'll introduce a PATCH DTO with optional fields.
 */
public record ExpenseUpdateRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "amount must have up to 17 digits and 2 decimals")
        BigDecimal amount,

        @NotNull(message = "date is required")
        LocalDate date,

        @NotNull(message = "category is required")
        ExpenseCategory category,

        @Size(max = 500, message = "note must be at most 500 characters")
        String note,

        @NotNull(message = "version is required")
        @PositiveOrZero(message = "version must be >= 0")
        Long version
) {
}
