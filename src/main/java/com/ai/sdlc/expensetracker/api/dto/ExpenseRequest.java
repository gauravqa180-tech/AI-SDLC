package com.ai.sdlc.expensetracker.api.dto;

import com.ai.sdlc.expensetracker.domain.ExpenseCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "amount must have up to 17 integer digits and 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "date is required")
        LocalDate date,

        @NotNull(message = "category is required")
        ExpenseCategory category,

        @Size(max = 1000, message = "note must be at most 1000 characters")
        String note
) {
}
