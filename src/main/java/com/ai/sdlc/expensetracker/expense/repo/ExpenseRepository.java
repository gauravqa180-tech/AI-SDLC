package com.ai.sdlc.expensetracker.expense.repo;

import com.ai.sdlc.expensetracker.expense.domain.Expense;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  List<Expense> findByDateBetween(LocalDate start, LocalDate end, Sort sort);

  List<Expense> findByCategoryIgnoreCaseAndDateBetween(String category, LocalDate start, LocalDate end, Sort sort);

  @Query("""
      select e from Expense e
      where e.date between :start and :end
        and (:category is null or lower(e.category) = lower(:category))
        and (:q is null or lower(e.note) like lower(concat('%', :q, '%')))
      """)
  List<Expense> search(
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("category") String category,
      @Param("q") String q,
      Sort sort);

  @Query("""
      select coalesce(sum(e.amount), 0)
      from Expense e
      where e.date between :start and :end
      """)
  java.math.BigDecimal sumAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

  @Query("""
      select e.category as category, coalesce(sum(e.amount), 0) as total
      from Expense e
      where e.date between :start and :end
      group by e.category
      order by total desc
      """)
  List<CategoryTotalProjection> totalsByCategory(@Param("start") LocalDate start, @Param("end") LocalDate end);

  interface CategoryTotalProjection {
    String getCategory();

    java.math.BigDecimal getTotal();
  }
}
