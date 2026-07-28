package com.example.expensetracker.service;

import com.example.expensetracker.domain.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class ExpenseSpecifications {

  private ExpenseSpecifications() {}

  public static Specification<Expense> dateBetween(LocalDate start, LocalDate end) {
    return (root, query, cb) -> cb.between(root.get("date"), start, end);
  }

  public static Specification<Expense> categoryEquals(String category) {
    return (root, query, cb) -> cb.equal(root.get("category"), category);
  }

  public static Specification<Expense> amountGte(BigDecimal min) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), min);
  }

  public static Specification<Expense> amountLte(BigDecimal max) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), max);
  }

  public static Specification<Expense> noteContainsIgnoreCase(String q) {
    return (root, query, cb) -> cb.like(cb.lower(root.get("note")), "%" + q.toLowerCase() + "%");
  }
}
