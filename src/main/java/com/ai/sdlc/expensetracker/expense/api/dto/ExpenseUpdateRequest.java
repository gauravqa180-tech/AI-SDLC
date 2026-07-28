package com.ai.sdlc.expensetracker.expense.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true, message = "amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "date is required")
        LocalDate date,

        @NotBlank(message = "category is required")
        @Size(max = 64, message = "category must be at most 64 characters")
        String category,

        @Size(max = 512, message = "note must be at most 512 characters")
        String note
) {
}
