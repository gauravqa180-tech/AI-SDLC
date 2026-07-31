package com.example.expensetracker.repository;

import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.domain.ExpenseCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            select e from Expense e
            where (:startDate is null or e.date >= :startDate)
              and (:endDate is null or e.date <= :endDate)
              and (:category is null or e.category = :category)
              and (:q is null or :q = '' or lower(coalesce(e.note,'')) like lower(concat('%', :q, '%')))
            """)
    List<Expense> search(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") ExpenseCategory category,
            @Param("q") String q,
            Sort sort);

    @Query("""
            select coalesce(sum(e.amount), 0) from Expense e
            where e.date >= :startDate and e.date <= :endDate
            """)
    BigDecimal sumAmountBetween(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

    @Query("""
            select coalesce(sum(e.amount), 0) from Expense e
            where e.date >= :startDate and e.date <= :endDate
              and e.category = :category
            """)
    BigDecimal sumAmountByCategoryBetweenDates(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("category") ExpenseCategory category);

    @Query("""
            select e.category, coalesce(sum(e.amount), 0)
            from Expense e
            where e.date >= :startDate and e.date <= :endDate
            group by e.category
            order by e.category
            """)
    List<Object[]> sumByCategoryBetween(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}
