package com.kan.expensetracker.repository;

import com.kan.expensetracker.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndDeletedFalse(Long id);
}
