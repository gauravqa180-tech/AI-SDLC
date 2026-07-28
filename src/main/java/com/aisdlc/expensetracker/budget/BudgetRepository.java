package com.aisdlc.expensetracker.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByMonthAndTypeAndCategory_Id(String month, BudgetType type, Long categoryId);
    Optional<Budget> findByMonthAndTypeAndCategoryIsNull(String month, BudgetType type);
    List<Budget> findByMonth(String month);
}
