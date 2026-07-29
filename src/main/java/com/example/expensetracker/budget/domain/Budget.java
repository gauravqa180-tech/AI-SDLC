package com.example.expensetracker.budget.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_month_category", columnNames = {"budgetMonth", "category"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stored as first day of month in DB via converter. */
    @Column(nullable = false)
    private YearMonth budgetMonth;

    /** null category represents overall monthly budget */
    @Column(length = 64)
    private String category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}
