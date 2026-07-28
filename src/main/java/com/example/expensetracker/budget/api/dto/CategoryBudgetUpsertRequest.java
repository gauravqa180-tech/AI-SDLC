package com.example.expensetracker.budget.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CategoryBudgetUpsertRequest(
    @NotBlank String month,
    @NotBlank @Size(max = 64) String category,
    @NotNull @DecimalMin("0.01") BigDecimal amount
) {}
