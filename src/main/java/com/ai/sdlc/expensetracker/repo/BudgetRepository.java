package com.ai.sdlc.expensetracker.repo;

import com.ai.sdlc.expensetracker.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByMonthAndCategory(String month, String category);

    List<Budget> findByMonth(String month);
}
