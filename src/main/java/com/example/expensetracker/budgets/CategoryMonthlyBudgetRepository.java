package com.example.expensetracker.budgets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface CategoryMonthlyBudgetRepository extends JpaRepository<CategoryMonthlyBudget, Long> {
    Optional<CategoryMonthlyBudget> findByBudgetMonthAndCategoryId(YearMonth month, Long categoryId);
    List<CategoryMonthlyBudget> findAllByBudgetMonth(YearMonth month);
    void deleteByBudgetMonthAndCategoryId(YearMonth month, Long categoryId);
}
