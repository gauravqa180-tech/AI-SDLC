package com.ai.sdlc.expensetracker.expense.service;

import com.ai.sdlc.expensetracker.expense.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ExpenseSpecifications {
    private ExpenseSpecifications() {
    }

    public static Specification<Expense> dateFrom(LocalDate from) {
        return (root, query, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("expenseDate"), from);
    }

    public static Specification<Expense> dateTo(LocalDate to) {
        return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("expenseDate"), to);
    }

    public static Specification<Expense> categoryEquals(String category) {
        return (root, query, cb) -> (category == null || category.isBlank())
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
    }

    public static Specification<Expense> noteContains(String q) {
        return (root, query, cb) -> (q == null || q.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("note")), "%" + q.trim().toLowerCase() + "%");
    }
}
