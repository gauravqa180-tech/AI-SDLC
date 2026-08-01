package com.aisdlc.expensetracker.expense.repository;

import com.aisdlc.expensetracker.expense.domain.Expense;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.expenseDate >= :start and e.expenseDate <= :end")
    java.math.BigDecimal sumAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
