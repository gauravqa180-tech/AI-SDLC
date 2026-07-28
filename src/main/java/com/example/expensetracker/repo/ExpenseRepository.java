package com.example.expensetracker.repo;

import com.example.expensetracker.domain.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

  @Query("select coalesce(sum(e.amount), 0) from Expense e where e.date between :start and :end")
  BigDecimal sumAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

  @Query("select e from Expense e where e.id = :id")
  Optional<Expense> findActiveById(@Param("id") Long id);

  @Query("select e.category as category, coalesce(sum(e.amount),0) as total "
      + "from Expense e where e.date between :start and :end group by e.category")
  List<CategoryTotalProjection> sumByCategoryBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

  @Query("select e from Expense e where e.deletedAt is not null")
  List<Expense> findDeleted(Sort sort);

  @Modifying
  @Query("update Expense e set e.deletedAt = null where e.id = :id")
  int restoreById(@Param("id") Long id);

  interface CategoryTotalProjection {
    String getCategory();

    BigDecimal getTotal();
  }
}
