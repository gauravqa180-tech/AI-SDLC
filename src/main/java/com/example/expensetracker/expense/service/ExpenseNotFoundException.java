package com.example.expensetracker.expense.service;

public class ExpenseNotFoundException extends RuntimeException {
    public ExpenseNotFoundException(Long id) {
        super("Expense not found: id=" + id);
    }
}
