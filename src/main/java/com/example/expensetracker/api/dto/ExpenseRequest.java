package com.example.expensetracker.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @NotNull
        LocalDate date,

        @NotBlank @Size(max = 64)
        String category,

        @Size(max = 255)
        String note,

        /**
         * Concurrency token from the last read.
         * Required for updates; optional for create.
         */
        Long version
) {
}
