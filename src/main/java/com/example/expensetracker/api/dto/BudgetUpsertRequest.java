package com.example.expensetracker.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BudgetUpsertRequest(
    /** Format: YYYY-MM */
    @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}") String month,
    /** Nullable/blank means overall budget. */
    @Size(max = 64) String category,
    @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount
) {}
