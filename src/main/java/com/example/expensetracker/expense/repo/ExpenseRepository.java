package com.example.expensetracker.expense.repo;

import com.example.expensetracker.expense.domain.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("""
            select e from Expense e
            where (:from is null or e.expenseDate >= :from)
              and (:to is null or e.expenseDate <= :to)
              and (:category is null or e.category = :category)
              and (:q is null or lower(e.note) like lower(concat('%', :q, '%')))
            """)
    Page<Expense> search(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") String category,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("""
            select e.category as category, sum(e.amount) as total
            from Expense e
            where (:from is null or e.expenseDate >= :from)
              and (:to is null or e.expenseDate <= :to)
            group by e.category
            order by sum(e.amount) desc
            """)
    List<CategoryTotalRow> totalsByCategory(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from Expense e
            where e.expenseDate >= :from and e.expenseDate <= :to
            """)
    java.math.BigDecimal totalForRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from Expense e
            where e.expenseDate >= :from and e.expenseDate <= :to
              and e.category = :category
            """)
    java.math.BigDecimal sumByCategoryAndRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") String category
    );

    interface CategoryTotalRow {
        String getCategory();

        java.math.BigDecimal getTotal();
    }
}
