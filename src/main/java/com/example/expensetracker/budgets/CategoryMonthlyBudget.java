package com.example.expensetracker.budgets;

import com.example.expensetracker.categories.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "category_monthly_budgets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_category_budget_month",
                columnNames = {"budgetMonth", "category_id"}
        ))
@Getter
@Setter
public class CategoryMonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private YearMonth budgetMonth;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
}
