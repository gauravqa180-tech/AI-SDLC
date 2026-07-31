package com.example.expensetracker.expenses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.deletedAt is null and e.expenseDate between :from and :to")
    BigDecimal sumBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.deletedAt is null and e.category.id = :categoryId and e.expenseDate between :from and :to")
    BigDecimal sumBetweenForCategory(@Param("categoryId") Long categoryId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
