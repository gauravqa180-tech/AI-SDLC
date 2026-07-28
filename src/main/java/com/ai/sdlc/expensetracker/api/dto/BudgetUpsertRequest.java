package com.ai.sdlc.expensetracker.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetUpsertRequest(
        /** YYYY-MM */
        @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}$") String month,
        /** null or blank means overall */
        @Size(max = 100) String category,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount
) {
}
