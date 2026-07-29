package com.ai.sdlc.expensetracker.repository;

import com.ai.sdlc.expensetracker.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByPeriodAndCategory(String period, String category);

    List<Budget> findByPeriod(String period);
}
