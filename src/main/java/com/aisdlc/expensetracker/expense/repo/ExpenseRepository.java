package com.aisdlc.expensetracker.expense.repo;

import com.aisdlc.expensetracker.expense.domain.Expense;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

  Page<Expense> findAllByDateBetween(LocalDate start, LocalDate end, Pageable pageable);
}
