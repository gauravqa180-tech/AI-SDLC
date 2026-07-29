package com.example.expensetracker.expense.repo;

import com.example.expensetracker.expense.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ExpenseSpecifications {
    private ExpenseSpecifications() {
    }

    public static Specification<Expense> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Expense> dateFrom(LocalDate from) {
        return (root, query, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("date"), from);
    }

    public static Specification<Expense> dateTo(LocalDate to) {
        return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("date"), to);
    }

    public static Specification<Expense> categoryEquals(String category) {
        return (root, query, cb) -> (category == null || category.isBlank()) ? cb.conjunction() : cb.equal(root.get("category"), category);
    }

    public static Specification<Expense> noteContains(String q) {
        return (root, query, cb) -> (q == null || q.isBlank()) ? cb.conjunction() : cb.like(cb.lower(root.get("note")), "%" + q.toLowerCase() + "%");
    }

    public static Specification<Expense> amountMin(BigDecimal min) {
        return (root, query, cb) -> min == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Expense> amountMax(BigDecimal max) {
        return (root, query, cb) -> max == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("amount"), max);
    }
}
