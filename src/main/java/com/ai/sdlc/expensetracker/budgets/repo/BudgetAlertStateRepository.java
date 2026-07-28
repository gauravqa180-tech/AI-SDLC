package com.ai.sdlc.expensetracker.budgets.repo;

import com.ai.sdlc.expensetracker.budgets.domain.BudgetAlertState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetAlertStateRepository extends JpaRepository<BudgetAlertState, Long> {

    Optional<BudgetAlertState> findByMonthAndCategory(String month, String category);
}
