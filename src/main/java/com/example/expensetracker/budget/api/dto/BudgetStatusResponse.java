package com.example.expensetracker.budget.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record BudgetStatusResponse(
    String month,
    BigDecimal totalBudget,
    BigDecimal totalSpent,
    BigDecimal totalRemaining,
    List<CategoryStatus> categories,
    List<String> alerts
) {
  public record CategoryStatus(String category, BigDecimal budget, BigDecimal spent, BigDecimal remaining) {}
}
