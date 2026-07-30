package com.ai.sdlc.expensetracker.budget.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BudgetUpsertRequest(
    @NotBlank String month, // yyyy-MM
    String category,
    @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount
) {
}
