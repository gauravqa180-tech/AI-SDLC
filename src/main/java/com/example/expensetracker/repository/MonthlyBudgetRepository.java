package com.example.expensetracker.repository;

import com.example.expensetracker.domain.ExpenseCategory;
import com.example.expensetracker.domain.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {

    Optional<MonthlyBudget> findByMonthAndCategory(String month, ExpenseCategory category);

    List<MonthlyBudget> findAllByMonth(String month);
}
