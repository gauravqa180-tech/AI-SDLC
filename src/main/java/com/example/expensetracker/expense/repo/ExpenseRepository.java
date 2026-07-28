package com.example.expensetracker.expense.repo;

import com.example.expensetracker.expense.domain.Expense;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

  @Query(
      "select coalesce(sum(e.amount), 0) from Expense e "
          + "where e.date >= :start and e.date <= :end")
  java.math.BigDecimal sumBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
