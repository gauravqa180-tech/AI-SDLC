package com.ai.sdlc.expensetracker.expense.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true, message = "amount must be > 0")
        BigDecimal amount,

        @NotNull
        LocalDate date,

        @NotBlank @Size(max = 80)
        String category,

        @Size(max = 500)
        String note
) {
}
