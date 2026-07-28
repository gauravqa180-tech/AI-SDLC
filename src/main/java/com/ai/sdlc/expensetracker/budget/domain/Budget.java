package com.ai.sdlc.expensetracker.budget.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budgets_month", columnNames = {"budgetMonth"})
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

    @Column(nullable = false)
    private YearMonth budgetMonth;

    @Column(precision = 12, scale = 2)
    private BigDecimal monthlyBudget;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CategoryBudget> categoryBudgets = new ArrayList<>();

    public void replaceCategoryBudgets(List<CategoryBudget> newOnes) {
        this.categoryBudgets.clear();
        for (CategoryBudget cb : newOnes) {
            cb.setBudget(this);
            this.categoryBudgets.add(cb);
        }
    }
}
