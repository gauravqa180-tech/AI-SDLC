package com.aisdlc.expensetracker.service;

import com.aisdlc.expensetracker.domain.Expense;
import com.aisdlc.expensetracker.domain.ExpenseCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    static Specification<Expense> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    static Specification<Expense> dateBetween(LocalDate start, LocalDate end) {
        if (start == null && end == null) return null;
        if (start != null && end != null) {
            return (root, query, cb) -> cb.between(root.get("expenseDate"), start, end);
        }
        if (start != null) {
            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), start);
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), end);
    }

    static Specification<Expense> noteContains(String contains) {
        if (contains == null || contains.isBlank()) return null;
        String like = "%" + contains.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("note")), like);
    }

    static Specification<Expense> categoryEquals(String category) {
        if (category == null || category.isBlank()) return null;
        ExpenseCategory parsed;
        try {
            parsed = ExpenseCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return (root, query, cb) -> cb.disjunction();
        }
        ExpenseCategory finalParsed = parsed;
        return (root, query, cb) -> cb.equal(root.get("category"), finalParsed);
    }

    static Specification<Expense> amountBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        if (min != null && max != null) {
            return (root, query, cb) -> cb.between(root.get("amount"), min, max);
        }
        if (min != null) {
            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), min);
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    static Sort sort(String sortBy, String sortDir) {
        String by = (sortBy == null || sortBy.isBlank()) ? "expenseDate" : sortBy;
        Sort.Direction dir = (sortDir == null || sortDir.isBlank()) ? Sort.Direction.DESC : Sort.Direction.fromString(sortDir);

        if (!by.equals("expenseDate") && !by.equals("amount")) {
            by = "expenseDate";
        }
        return Sort.by(dir, by);
    }
}
