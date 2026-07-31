package com.example.expensetracker.budgets;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "overall_monthly_budgets",
        uniqueConstraints = @UniqueConstraint(name = "uk_overall_budget_month", columnNames = {"budgetMonth"}))
@Getter
@Setter
public class OverallMonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private YearMonth budgetMonth;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
}
