package com.example.expensetracker.expense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "budgets",
        uniqueConstraints = @UniqueConstraint(name = "uk_budget_month_category", columnNames = {"budget_month", "category"}),
        indexes = {
                @Index(name = "idx_budget_month", columnList = "budget_month"),
                @Index(name = "idx_budget_category", columnList = "category")
        })
@Getter
@Setter
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_month", nullable = false)
    private YearMonth month;

    @Column(name = "category", length = 64)
    private String category; // null = overall

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "warn_threshold_pct", precision = 5, scale = 4)
    private BigDecimal warnThresholdPct; // null => default 0.8
}
