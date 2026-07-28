package com.ai.sdlc.expensetracker.expenses.service;

import com.ai.sdlc.expensetracker.expenses.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> all(Optional<String> q,
                                            Optional<LocalDate> dateFrom,
                                            Optional<LocalDate> dateTo,
                                            Optional<String> category,
                                            Optional<BigDecimal> minAmount,
                                            Optional<BigDecimal> maxAmount) {
        return Specification.where(noteContains(q))
                .and(dateGte(dateFrom))
                .and(dateLte(dateTo))
                .and(categoryEquals(category))
                .and(amountGte(minAmount))
                .and(amountLte(maxAmount));
    }

    private static Specification<Expense> noteContains(Optional<String> q) {
        return (root, query, cb) -> q.filter(s -> !s.isBlank())
                .map(s -> cb.like(cb.lower(root.get("note")), "%" + s.toLowerCase() + "%"))
                .orElseGet(cb::conjunction);
    }

    private static Specification<Expense> dateGte(Optional<LocalDate> from) {
        return (root, query, cb) -> from
                .map(d -> cb.greaterThanOrEqualTo(root.get("date"), d))
                .orElseGet(cb::conjunction);
    }

    private static Specification<Expense> dateLte(Optional<LocalDate> to) {
        return (root, query, cb) -> to
                .map(d -> cb.lessThanOrEqualTo(root.get("date"), d))
                .orElseGet(cb::conjunction);
    }

    private static Specification<Expense> categoryEquals(Optional<String> category) {
        return (root, query, cb) -> category.filter(s -> !s.isBlank())
                .map(s -> cb.equal(cb.lower(root.get("category")), s.toLowerCase()))
                .orElseGet(cb::conjunction);
    }

    private static Specification<Expense> amountGte(Optional<BigDecimal> min) {
        return (root, query, cb) -> min
                .map(a -> cb.greaterThanOrEqualTo(root.get("amount"), a))
                .orElseGet(cb::conjunction);
    }

    private static Specification<Expense> amountLte(Optional<BigDecimal> max) {
        return (root, query, cb) -> max
                .map(a -> cb.lessThanOrEqualTo(root.get("amount"), a))
                .orElseGet(cb::conjunction);
    }
}
