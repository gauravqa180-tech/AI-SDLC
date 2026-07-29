package com.example.expensetracker.budget.api.dto;

public record BudgetAlertResponse(
        String month,
        String category,
        int thresholdPercent,
        String message
) {
}
