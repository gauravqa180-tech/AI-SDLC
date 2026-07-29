package com.aisdlc.expensetracker.expense.service;

import com.aisdlc.expensetracker.expense.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ExpenseSpecifications {
    private ExpenseSpecifications() {}

    public static Specification<Expense> isActive() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Expense> dateGte(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), from);
    }

    public static Specification<Expense> dateLte(LocalDate to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), to);
    }

    public static Specification<Expense> categoryEquals(String category) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<Expense> noteContains(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("note")), like);
    }
}
