package com.example.expensetracker.api.dto;

import java.math.BigDecimal;

public record CategoryTotalResponse(
    String category,
    BigDecimal total
) {}
