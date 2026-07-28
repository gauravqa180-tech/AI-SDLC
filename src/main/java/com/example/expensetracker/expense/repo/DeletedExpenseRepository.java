package com.example.expensetracker.expense.repo;

import com.example.expensetracker.expense.domain.DeletedExpense;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedExpenseRepository extends JpaRepository<DeletedExpense, Long> {
  List<DeletedExpense> findByDeletedAtBefore(OffsetDateTime cutoff);
}
