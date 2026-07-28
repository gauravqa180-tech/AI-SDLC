package com.kan.expensetracker.budget.repo;

import com.kan.expensetracker.budget.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByBudgetMonthAndCategory_Id(String budgetMonth, Long categoryId);

    Optional<Budget> findByBudgetMonthAndCategoryIsNull(String budgetMonth);

    List<Budget> findAllByBudgetMonth(String budgetMonth);
}
