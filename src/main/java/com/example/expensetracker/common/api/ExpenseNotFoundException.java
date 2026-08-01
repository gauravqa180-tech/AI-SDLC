package com.example.expensetracker.common.api;

public class ExpenseNotFoundException extends RuntimeException {
    public ExpenseNotFoundException(Long id) {
        super("Expense not found: " + id);
    }
}
