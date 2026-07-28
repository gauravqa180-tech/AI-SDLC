package com.ai.sdlc.expensetracker.expense.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseCreateRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true, message = "amount must be > 0")
        @Digits(integer = 10, fraction = 2, message = "amount must have up to 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "expenseDate is required")
        LocalDate expenseDate,

        @NotNull(message = "categoryId is required")
        Long categoryId,

        @Size(max = 500, message = "note must be <= 500 characters")
        String note
) {}
