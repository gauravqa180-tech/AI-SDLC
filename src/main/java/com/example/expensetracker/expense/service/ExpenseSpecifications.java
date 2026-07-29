package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            if (start != null && end != null) return cb.between(root.get("date"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("date"), start);
            return cb.lessThanOrEqualTo(root.get("date"), end);
        };
    }

    public static Specification<Expense> categoryEquals(String category) {
        return (root, query, cb) -> {
            if (category == null || category.isBlank()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("category")), category.toLowerCase());
        };
    }

    public static Specification<Expense> noteContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("note")), "%" + q.toLowerCase() + "%");
        };
    }
}
