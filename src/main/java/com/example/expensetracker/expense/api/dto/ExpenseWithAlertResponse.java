package com.example.expensetracker.expense.api.dto;

import com.example.expensetracker.budget.api.dto.BudgetAlertResponse;

public record ExpenseWithAlertResponse(
        ExpenseResponse expense,
        BudgetAlertResponse alert
) {
}
