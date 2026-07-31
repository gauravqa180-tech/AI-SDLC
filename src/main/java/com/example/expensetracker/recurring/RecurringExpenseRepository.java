package com.example.expensetracker.recurring;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
}
