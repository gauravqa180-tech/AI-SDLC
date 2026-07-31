package com.ai.sdlc.expensetracker.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) @Digits(integer = 11, fraction = 2) BigDecimal amount,
        @NotNull LocalDate date,
        @NotBlank @Size(max = 100) String category,
        @Size(max = 500) String note
) {
}
