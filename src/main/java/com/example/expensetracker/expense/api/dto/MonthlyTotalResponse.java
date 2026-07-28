package com.example.expensetracker.expense.api.dto;

import java.math.BigDecimal;

public record MonthlyTotalResponse(String month, BigDecimal total) {}
