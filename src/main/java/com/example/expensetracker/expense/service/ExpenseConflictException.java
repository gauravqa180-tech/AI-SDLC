package com.example.expensetracker.expense.service;

import lombok.Getter;

@Getter
public class ExpenseConflictException extends RuntimeException {

    private final Long id;
    private final Long currentVersion;
    private final Long providedVersion;

    public ExpenseConflictException(Long id, Long currentVersion, Long providedVersion) {
        super("Expense update conflict for id=" + id + ": currentVersion=" + currentVersion + ", providedVersion=" + providedVersion);
        this.id = id;
        this.currentVersion = currentVersion;
        this.providedVersion = providedVersion;
    }
}
