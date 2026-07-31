package com.example.expensetracker.expense;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from Expense e
            where e.deletedAt is null
              and e.expenseDate >= :start
              and e.expenseDate <= :end
            """)
    BigDecimal sumForDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    List<Expense> findAll(Specification<Expense> spec);
}
