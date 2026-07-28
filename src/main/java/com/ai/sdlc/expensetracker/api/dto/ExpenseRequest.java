package com.ai.sdlc.expensetracker.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull LocalDate date,
        @NotBlank @Size(max = 64) String category,
        @Size(max = 255) String note
) {
}
