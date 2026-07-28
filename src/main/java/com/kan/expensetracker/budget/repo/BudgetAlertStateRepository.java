package com.kan.expensetracker.budget.repo;

import com.kan.expensetracker.budget.domain.BudgetAlertState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetAlertStateRepository extends JpaRepository<BudgetAlertState, Long> {
    Optional<BudgetAlertState> findByBudgetMonthAndCategory_IdAndThresholdType(String budgetMonth, Long categoryId, BudgetAlertState.ThresholdType thresholdType);

    Optional<BudgetAlertState> findByBudgetMonthAndCategoryIsNullAndThresholdType(String budgetMonth, BudgetAlertState.ThresholdType thresholdType);
}
