package com.example.expensetracker.expense.repo;

import com.example.expensetracker.expense.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByMonthAndCategory(YearMonth month, String category);

    List<Budget> findByMonth(YearMonth month);
}
