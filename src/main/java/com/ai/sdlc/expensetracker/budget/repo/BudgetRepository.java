package com.ai.sdlc.expensetracker.budget.repo;

import com.ai.sdlc.expensetracker.budget.domain.Budget;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

  Optional<Budget> findByMonthAndCategory(YearMonth month, String category);

  Optional<Budget> findByMonthAndCategoryIsNull(YearMonth month);

  List<Budget> findAllByMonth(YearMonth month);
}
