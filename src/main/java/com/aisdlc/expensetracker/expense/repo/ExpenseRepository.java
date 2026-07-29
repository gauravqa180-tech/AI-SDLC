package com.aisdlc.expensetracker.expense.repo;

import com.aisdlc.expensetracker.expense.domain.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @Query("select coalesce(sum(e.amount), 0) from Expense e " +
            "where e.deleted = false and e.expenseDate >= :from and e.expenseDate <= :to")
    BigDecimal sumBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select e from Expense e where e.deleted = false")
    Page<Expense> findAllActive(Pageable pageable);
}
