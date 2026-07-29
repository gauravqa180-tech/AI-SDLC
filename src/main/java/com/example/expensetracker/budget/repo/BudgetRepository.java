package com.example.expensetracker.budget.repo;

import com.example.expensetracker.budget.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByBudgetMonthAndCategory(YearMonth budgetMonth, String category);

    List<Budget> findAllByBudgetMonth(YearMonth budgetMonth);
}
