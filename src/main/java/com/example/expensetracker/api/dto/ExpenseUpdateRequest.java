package com.example.expensetracker.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateRequest(
    @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
    @NotNull LocalDate date,
    @NotBlank @Size(max = 64) String category,
    @Size(max = 500) String note,
    /** Optimistic lock version. If provided and mismatched, update fails with 409. */
    Long version
) {}
