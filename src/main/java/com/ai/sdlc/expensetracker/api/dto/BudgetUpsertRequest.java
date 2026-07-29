package com.ai.sdlc.expensetracker.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetUpsertRequest(
        @NotBlank String period,
        @Size(max = 100) String category,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal limitAmount,
        @NotNull @DecimalMin(value = "0", inclusive = true) @DecimalMax(value = "100", inclusive = true) BigDecimal warnThresholdPercent
) {
}
