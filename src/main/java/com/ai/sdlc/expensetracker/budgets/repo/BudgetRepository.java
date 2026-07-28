package com.ai.sdlc.expensetracker.budgets.repo;

import com.ai.sdlc.expensetracker.budgets.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByMonth(String month);

    Optional<Budget> findByMonthAndCategory(String month, String category);
}
