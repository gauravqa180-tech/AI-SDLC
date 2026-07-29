package com.example.expensetracker.budget.repo;

import com.example.expensetracker.budget.domain.BudgetAlertState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetAlertStateRepository extends JpaRepository<BudgetAlertState, Long> {
    Optional<BudgetAlertState> findByBudgetMonthAndCategory(String budgetMonth, String category);
}
