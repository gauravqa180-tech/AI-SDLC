package com.ai.sdlc.expensetracker.budget.repo;

import com.ai.sdlc.expensetracker.budget.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByBudgetMonth(YearMonth month);

    default Optional<Budget> findByMonth(YearMonth month) {
        return findByBudgetMonth(month);
    }
}
