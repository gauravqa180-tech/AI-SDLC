package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> dateFrom(LocalDate from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("date"), from);
    }

    public static Specification<Expense> dateTo(LocalDate to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("date"), to);
    }

    public static Specification<Expense> categoryEquals(String category) {
        return (root, query, cb) -> (category == null || category.isBlank()) ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Expense> noteContainsIgnoreCase(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            return cb.like(cb.lower(root.get("note")), "%" + q.toLowerCase() + "%");
        };
    }
}
