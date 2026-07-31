package com.example.expensetracker.expense;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Expense> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> cb.between(root.get("expenseDate"), start, end);
    }

    public static Specification<Expense> categoryIdEquals(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Expense> noteContains(String term) {
        String like = "%" + term.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("note")), like);
    }

    public static Specification<Expense> amountGte(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Expense> amountLte(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), max);
    }
}
