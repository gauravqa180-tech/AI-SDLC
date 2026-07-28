package com.ai.sdlc.expensetracker.repo;

import com.ai.sdlc.expensetracker.domain.Expense;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.date >= :from and e.date <= :to")
    BigDecimal sumAmountBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select e.category as category, coalesce(sum(e.amount), 0) as total from Expense e where e.date >= :from and e.date <= :to group by e.category")
    List<Object[]> sumByCategoryBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<Expense> findByDateBetween(LocalDate from, LocalDate to, Sort sort);
}
