package com.example.expensetracker.budget.repo;

import com.example.expensetracker.budget.domain.CategoryBudget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {
  Optional<CategoryBudget> findByMonthAndCategory(String month, String category);
  List<CategoryBudget> findByMonth(String month);
}
