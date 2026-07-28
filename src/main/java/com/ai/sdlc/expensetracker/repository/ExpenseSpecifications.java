package com.ai.sdlc.expensetracker.repository;

import com.ai.sdlc.expensetracker.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Expense> dateFrom(LocalDate from) {
        return (root, query, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("expenseDate"), from);
    }

    public static Specification<Expense> dateTo(LocalDate to) {
        return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("expenseDate"), to);
    }

    public static Specification<Expense> categoryEquals(String category) {
        return (root, query, cb) -> (category == null || category.isBlank()) ? cb.conjunction() : cb.equal(root.get("category"), category);
    }

    public static Specification<Expense> amountMin(BigDecimal min) {
        return (root, query, cb) -> min == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Expense> amountMax(BigDecimal max) {
        return (root, query, cb) -> max == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    public static Specification<Expense> noteContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("note")), "%" + q.toLowerCase() + "%");
        };
    }
}
