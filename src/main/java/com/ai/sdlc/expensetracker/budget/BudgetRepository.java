package com.ai.sdlc.expensetracker.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByYearAndMonth(int year, int month);
}
