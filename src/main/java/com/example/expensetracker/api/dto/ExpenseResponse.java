package com.example.expensetracker.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        String category,
        String note
) {
}
