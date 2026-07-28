package com.ai.sdlc.expensetracker.expense.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateExpenseRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "amount must have up to 17 integer digits and 2 fraction digits")
        BigDecimal amount,

        @NotNull(message = "expenseDate is required")
        LocalDate expenseDate,

        @NotBlank(message = "category is required")
        @Size(max = 64, message = "category must be at most 64 characters")
        String category,

        @Size(max = 500, message = "note must be at most 500 characters")
        String note
) {}
