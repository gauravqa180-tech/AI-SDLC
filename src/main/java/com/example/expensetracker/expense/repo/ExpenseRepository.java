package com.example.expensetracker.expense.repo;

import com.example.expensetracker.expense.domain.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndDeletedFalse(Long id);

    @Query("select coalesce(sum(e.amount), 0) from Expense e " +
            "where e.deleted = false and e.date >= :start and e.date <= :end")
    BigDecimal sumAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    Page<Expense> findAllByDeletedFalse(Pageable pageable);
}
