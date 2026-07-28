package com.ai.sdlc.expensetracker.budget;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uq_budget_month", columnNames = {"budget_year", "budget_month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_year", nullable = false)
    private int year;

    @Column(name = "budget_month", nullable = false)
    private int month;

    @Column(name = "overall_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal overallAmount;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CategoryBudget> categoryBudgets = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
