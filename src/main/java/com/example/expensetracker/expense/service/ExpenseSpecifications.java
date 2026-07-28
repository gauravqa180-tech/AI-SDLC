package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.domain.Expense;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

final class ExpenseSpecifications {

  private ExpenseSpecifications() {}

  static Specification<Expense> dateGte(LocalDate start) {
    return (root, query, cb) -> start == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("date"), start);
  }

  static Specification<Expense> dateLte(LocalDate end) {
    return (root, query, cb) -> end == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("date"), end);
  }

  static Specification<Expense> categoryEq(String category) {
    return (root, query, cb) -> (category == null || category.isBlank())
        ? cb.conjunction()
        : cb.equal(root.get("category"), category);
  }

  static Specification<Expense> noteContains(String q) {
    return (root, query, cb) -> (q == null || q.isBlank())
        ? cb.conjunction()
        : cb.like(cb.lower(root.get("note")), "%" + q.toLowerCase() + "%");
  }
}
