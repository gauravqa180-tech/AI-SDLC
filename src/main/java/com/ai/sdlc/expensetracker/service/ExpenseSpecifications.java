package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ExpenseSpecifications {
    private ExpenseSpecifications() {
    }

    public static Specification<Expense> dateGte(LocalDate start) {
        return (root, query, cb) -> start == null ? null : cb.greaterThanOrEqualTo(root.get("date"), start);
    }

    public static Specification<Expense> dateLte(LocalDate end) {
        return (root, query, cb) -> end == null ? null : cb.lessThanOrEqualTo(root.get("date"), end);
    }

    public static Specification<Expense> categoryEq(String category) {
        return (root, query, cb) -> (category == null || category.isBlank()) ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Expense> amountGte(BigDecimal min) {
        return (root, query, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Expense> amountLte(BigDecimal max) {
        return (root, query, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    public static Specification<Expense> search(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("note")), like),
                    cb.like(cb.lower(root.get("category")), like),
                    cb.like(cb.function("CAST", String.class, root.get("amount")), "%" + q.trim() + "%")
            );
        };
    }
}
