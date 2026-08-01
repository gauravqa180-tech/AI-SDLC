package com.ai.sdlc.expensetracker.expense.repo;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.ai.sdlc.expensetracker.expense.model.Expense;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Expense> categoryEquals(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Expense> dateGte(LocalDate start) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), start);
    }

    public static Specification<Expense> dateLte(LocalDate end) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), end);
    }

    public static Specification<Expense> noteContainsIgnoreCase(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("note")), like);
    }
}
