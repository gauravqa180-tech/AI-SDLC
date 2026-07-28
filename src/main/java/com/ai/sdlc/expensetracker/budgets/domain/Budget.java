package com.ai.sdlc.expensetracker.budgets.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "budgets",
        uniqueConstraints = @UniqueConstraint(name = "uk_budget_month_category", columnNames = {"budget_month", "category"}),
        indexes = @Index(name = "idx_budget_month", columnList = "budget_month")
)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_month", nullable = false, length = 7)
    private String month; // YYYY-MM

    @Column(name = "category", length = 100)
    private String category; // null => overall

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    protected Budget() {
    }

    public Budget(YearMonth month, String category, BigDecimal amount) {
        this.month = month.toString();
        this.category = category;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public String getMonth() {
        return month;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
