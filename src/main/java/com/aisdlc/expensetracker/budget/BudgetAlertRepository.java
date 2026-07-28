package com.aisdlc.expensetracker.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {
    Optional<BudgetAlert> findByMonthAndTypeAndCategoryIdAndThreshold(String month, BudgetType type, Long categoryId, int threshold);
}
