package com.aisdlc.expensetracker.service;

import com.aisdlc.expensetracker.domain.Expense;
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
        return (root, query, cb) -> cb.between(root.get("date"), start, end);
    }

    public static Specification<Expense> categoryEquals(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Expense> amountGte(BigDecimal minAmount) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Expense> amountLte(BigDecimal maxAmount) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }

    public static Specification<Expense> noteContainsIgnoreCase(String q) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("note")), "%" + q.toLowerCase() + "%");
    }
}
