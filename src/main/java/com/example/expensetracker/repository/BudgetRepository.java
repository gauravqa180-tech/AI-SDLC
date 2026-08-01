package com.example.expensetracker.repository;

import com.example.expensetracker.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByMonthAndCategory(String month, String category);

    List<Budget> findByMonth(String month);
}
