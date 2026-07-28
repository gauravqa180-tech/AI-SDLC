package com.example.expensetracker.expense.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
    @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
    @NotNull LocalDate date,
    @NotBlank @Size(max = 64) String category,
    @Size(max = 255) String note
) {}
