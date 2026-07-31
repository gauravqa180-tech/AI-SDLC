package com.example.expensetracker.api.dto;

import com.example.expensetracker.domain.ExpenseCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount must be > 0")
        BigDecimal amount,

        @NotNull(message = "date is required")
        LocalDate date,

        @NotNull(message = "category is required")
        ExpenseCategory category,

        @Size(max = 500, message = "note must be <= 500 chars")
        String note
) {
}
