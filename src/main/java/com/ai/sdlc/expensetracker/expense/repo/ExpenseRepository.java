package com.ai.sdlc.expensetracker.expense.repo;

import com.ai.sdlc.expensetracker.expense.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
