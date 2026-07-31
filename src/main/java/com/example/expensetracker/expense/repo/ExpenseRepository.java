package com.example.expensetracker.expense.repo;

import com.example.expensetracker.expense.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.date between :start and :end")
    BigDecimal sumAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
