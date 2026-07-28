package com.example.expensetracker.repo;

import com.example.expensetracker.domain.Budget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

  List<Budget> findByMonth(String month);

  Optional<Budget> findByMonthAndCategory(String month, String category);
}
