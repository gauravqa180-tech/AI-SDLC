package com.example.expensetracker.expenses;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ExpenseSpecifications {
    private ExpenseSpecifications() {}

    public static Specification<Expense> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Expense> categoryIdEquals(Long categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Expense> dateBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return null;
        return (root, query, cb) -> cb.between(root.get("expenseDate"), from, to);
    }

    public static Specification<Expense> noteContainsIgnoreCase(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("note")), like);
    }
}
