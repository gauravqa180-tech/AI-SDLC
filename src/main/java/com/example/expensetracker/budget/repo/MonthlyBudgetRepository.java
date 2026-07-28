package com.example.expensetracker.budget.repo;

import com.example.expensetracker.budget.domain.MonthlyBudget;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
  Optional<MonthlyBudget> findByMonth(String month);
}
