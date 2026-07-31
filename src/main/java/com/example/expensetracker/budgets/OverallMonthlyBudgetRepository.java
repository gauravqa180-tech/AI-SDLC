package com.example.expensetracker.budgets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Optional;

public interface OverallMonthlyBudgetRepository extends JpaRepository<OverallMonthlyBudget, Long> {
    Optional<OverallMonthlyBudget> findByBudgetMonth(YearMonth month);
    void deleteByBudgetMonth(YearMonth month);
}
